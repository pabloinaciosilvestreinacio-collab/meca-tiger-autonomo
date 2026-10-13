import json
import math
from pathlib import Path
import sys
import threading
import time
import unittest
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from http.server import ThreadingHTTPServer

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from physics import World, rotate, trajectory
from server import Session, build_world, executable, App, handler_for

ROOT = Path(__file__).resolve().parents[1]


class PhysicsTests(unittest.TestCase):
    def world(self):
        w = World(x=0,y=0)
        w.motors["MD"].direction = -1
        return w

    def test_straight_one_meter(self):
        w = self.world()
        w.motors["ME"].power = w.motors["MD"].power = .5
        for _ in range(200): w.step(.01)
        self.assertAlmostEqual(w.x,0,places=10)
        self.assertAlmostEqual(w.y,1,places=10)
        self.assertEqual(w.motors["ME"].position(),1981)
        self.assertEqual(w.motors["MD"].position(),1981)

    def test_reverse(self):
        w=self.world()
        w.motors["ME"].power=w.motors["MD"].power=-.5
        for _ in range(100): w.step(.01)
        self.assertAlmostEqual(w.y,-.5)
        self.assertLess(w.motors["MD"].position(),0)

    def test_rotation_around_center(self):
        w=self.world();w.motors["ME"].power=-.3;w.motors["MD"].power=.3
        for _ in range(100): w.step(.01)
        self.assertAlmostEqual(w.x,0);self.assertAlmostEqual(w.y,0)
        self.assertAlmostEqual(w.yaw,.6/.45)

    def test_exact_arc(self):
        w=self.world();w.motors["ME"].power=.3;w.motors["MD"].power=.6
        for _ in range(100): w.step(.01)
        radius=.45/(.3/.45);angle=.3/.45
        self.assertAlmostEqual(w.x,radius*(math.cos(angle)-1))
        self.assertAlmostEqual(w.y,radius*math.sin(angle))

    def test_diagonal_requires_heading(self):
        w=self.world();w.yaw=-math.pi/4
        w.motors["ME"].power=w.motors["MD"].power=.5
        for _ in range(100):w.step(.01)
        self.assertAlmostEqual(w.x,w.y)
        self.assertAlmostEqual(w.x,.5/math.sqrt(2))

    def test_encoder_direction_and_reset(self):
        s=Session();s.rpc("motor","MD","direction","REVERSE")
        s.rpc("motor","MD","power",".5");s.rpc("motor","ME","power",".5")
        s.world.step(.1)
        self.assertGreater(float(s.rpc("motor","MD","encoder")),0)
        s.rpc("motor","MD","mode","STOP_AND_RESET_ENCODER")
        self.assertEqual(s.rpc("motor","MD","encoder"),"0")
        self.assertEqual(s.world.motors["MD"].power,0)

    def test_run_to_position_both_directions(self):
        for target in [200,-200]:
            s=Session();s.rpc("motor","MD","direction","REVERSE")
            s.rpc("motor","MD","target",str(target));s.rpc("motor","MD","mode","RUN_TO_POSITION");s.rpc("motor","MD","power",".7")
            for _ in range(200):s.world.step(.01)
            self.assertAlmostEqual(s.world.motors["MD"].position(),target,delta=2)
            self.assertEqual(s.rpc("motor","MD","busy"),"false")

    def test_yaw_wrap_and_zero(self):
        s=Session();s.world.yaw=math.radians(179);s.rpc("resetYaw")
        s.world.yaw=math.radians(-179)
        self.assertAlmostEqual(float(s.rpc("yaw")),2)

    def test_launcher_rotation_preserves_radius(self):
        for angle in [0,30,90,180,-179]:
            x,y=rotate(.1125,.225,math.radians(angle))
            self.assertAlmostEqual(math.hypot(x,y),math.hypot(.1125,.225))

    def test_ballistics_known_result(self):
        self.assertAlmostEqual(trajectory(0,5,45,25/9.81),0,places=10)
        self.assertAlmostEqual(trajectory(.3,5,45,0),.3)

    def test_invalid_configuration(self):
        for config in [{"track":0},{"max_speed":float('nan')},{"left":"MD"},{"yaw":float('inf')}]:
            with self.assertRaises(ValueError): build_world(config)

    def test_power_clamp_nan_and_stop(self):
        s=Session();s.rpc("motor","ME","power","2")
        self.assertEqual(s.world.motors["ME"].power,1)
        with self.assertRaises(ValueError):s.rpc("motor","ME","power","NaN")
        s.stop();self.assertTrue(all(m.power==0 for m in s.world.motors.values()))


class HTTPTests(unittest.TestCase):
    def setUp(self):
        self.app=App();self.http=ThreadingHTTPServer(('127.0.0.1',0),handler_for(self.app))
        self.thread=threading.Thread(target=self.http.serve_forever,daemon=True);self.thread.start()
        self.url=f'http://127.0.0.1:{self.http.server_port}'

    def tearDown(self):
        self.app.session.stop();self.http.shutdown();self.http.server_close();self.thread.join()

    def test_static_and_state(self):
        for path in ['/', '/app.js','/model.mjs','/style.css','/examples','/api/state']:
            with urlopen(self.url+path) as r:self.assertEqual(r.status,200)

    def test_mutation_requires_token(self):
        request=Request(self.url+'/api/stop',data=b'{}',headers={'Content-Type':'application/json'})
        with self.assertRaises(HTTPError) as error:urlopen(request)
        self.assertEqual(error.exception.code,403)

    def test_gamepad_two_players(self):
        p1=[0]*20;p1[1]=-.75;p1[6]=1;p2=[0]*20;p2[4]=.6
        req=Request(self.url+'/api/gamepad',data=json.dumps({'1':p1,'2':p2}).encode(),headers={'X-Sim-Token':self.app.token,'Content-Type':'application/json'})
        with urlopen(req) as r:self.assertEqual(r.status,200)
        self.assertEqual(self.app.session.gamepads[0][1],-.75)
        self.assertEqual(self.app.session.gamepads[1][4],.6)


@unittest.skipUnless(executable('javac') and executable('java'),'JDK requerido para testes Java reais')
class JavaTests(unittest.TestCase):
    def run_java(self, source, timeout=12):
        s=Session({'duration':8});s.run(source)
        deadline=time.monotonic()+timeout
        while s.status not in ('inicializando','erro') and time.monotonic()<deadline:time.sleep(.01)
        self.assertEqual(s.status,'inicializando',s.logs)
        s.started=True;s.status='executando'
        return s,deadline

    def finish(self,s,deadline):
        s.worker.join(max(0,deadline-time.monotonic()))
        if s.worker.is_alive():s.stop();s.worker.join(2);self.fail('Java não encerrou no prazo')
        self.assertEqual(s.status,'concluído',s.logs)

    def test_autonomous_real_java_encoder_imu(self):
        source=(ROOT/'examples/AutonomoDemo.java').read_text(encoding='utf-8')
        s,deadline=self.run_java(source)
        self.finish(s,deadline)
        self.assertAlmostEqual(math.degrees(s.world.yaw),50,delta=1.5)
        self.assertAlmostEqual(s.world.x,1.83-.6*math.sin(math.radians(50)),delta=.04)
        self.assertAlmostEqual(s.world.y,.65+1+.6*math.cos(math.radians(50)),delta=.04)
        self.assertTrue(all(m.power==0 for m in s.world.motors.values()))

    def test_teleop_gamepad_and_stop(self):
        source=(ROOT/'examples/TeleOpDemo.java').read_text(encoding='utf-8')
        s,deadline=self.run_java(source)
        s.gamepads[0][1]=-.6
        time.sleep(.25)
        self.assertGreater(s.world.y,.70)
        self.assertGreater(s.world.motors['ME'].power,0)
        s.gamepads[0]=[0]*20;time.sleep(.07)
        self.assertEqual(s.world.motors['ME'].power,0)
        s.stop();s.worker.join(2)
        self.assertFalse(s.worker.is_alive())
        self.assertTrue(all(m.power==0 for m in s.world.motors.values()))

    def test_compilation_failure_is_visible(self):
        s=Session();s.run('public class Bad extends LinearOpMode { broken }');s.worker.join(10)
        self.assertEqual(s.status,'erro');self.assertIn('error:',s.logs)

    def test_stop_kills_infinite_java_loop(self):
        s,deadline=self.run_java('import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode; public class Loop extends LinearOpMode {public void runOpMode() throws InterruptedException {waitForStart();while(true){}}}')
        time.sleep(.05);s.stop();s.worker.join(2)
        self.assertFalse(s.worker.is_alive())

    def test_missing_hardware_fails(self):
        source='import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode; import com.qualcomm.robotcore.hardware.DcMotor; public class BadHardware extends LinearOpMode {public void runOpMode(){hardwareMap.get(DcMotor.class,"MISSING");}}'
        s=Session();s.run(source);s.worker.join(10)
        self.assertEqual(s.status,'erro');self.assertIn('Hardware não configurado',s.logs)

    def test_sleep_during_init(self):
        source='import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode; public class InitSleep extends LinearOpMode {public void runOpMode(){sleep(100);telemetry.addLine("init completo");telemetry.update();waitForStart();}}'
        s=Session();s.run(source)
        deadline=time.monotonic()+10
        while 'init completo' not in s.telemetry and s.status!='erro' and time.monotonic()<deadline:time.sleep(.01)
        self.assertIn('init completo',s.telemetry,s.logs)
        self.assertEqual(s.world.elapsed,0)
        s.started=True;s.status='executando';self.finish(s,deadline)

    def test_duration_stops_uncooperative_java(self):
        source='import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode; public class DurationLoop extends LinearOpMode {public void runOpMode(){waitForStart();while(true){}}}'
        s=Session({'duration':1});s.run(source)
        deadline=time.monotonic()+10
        while s.status not in ('inicializando','erro') and time.monotonic()<deadline:time.sleep(.01)
        self.assertEqual(s.status,'inicializando',s.logs)
        s.started=True;s.status='executando';s.worker.join(3)
        self.assertFalse(s.worker.is_alive());self.assertEqual(s.status,'parado')
        self.assertAlmostEqual(s.world.elapsed,1,delta=.02)


if __name__=='__main__':unittest.main(verbosity=2)
