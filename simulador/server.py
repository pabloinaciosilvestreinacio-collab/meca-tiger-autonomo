"""Servidor local e execução REAL de Java com hardware FTC idealizado."""
import base64
import json
import math
import os
from pathlib import Path
import re
import secrets
import shutil
import subprocess
import tempfile
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse

from physics import World, Motor
from sdk import write_sdk

ROOT = Path(__file__).resolve().parent


def executable(name):
    home = os.environ.get("JAVA_HOME")
    candidate = Path(home) / "bin" / (name + (".exe" if os.name == "nt" else "")) if home else None
    return str(candidate) if candidate and candidate.is_file() else shutil.which(name)


def build_world(config):
    world = World()
    bounds = {"track": (.05, 2), "width": (.05, 2), "length": (.05, 2),
              "max_speed": (.05, 10), "ticks_per_m": (1, 100000), "x": (-10, 10), "y": (-10, 10)}
    for key, (low, high) in bounds.items():
        value = float(config.get(key, getattr(world, key)))
        if not math.isfinite(value) or not low <= value <= high:
            raise ValueError(f"{key}: valor deve estar entre {low} e {high}")
        setattr(world, key, value)
    yaw = float(config.get("yaw", 0))
    if not math.isfinite(yaw):
        raise ValueError("Yaw inválido")
    world.yaw = math.radians(yaw)
    left, right = str(config.get("left", "ME")), str(config.get("right", "MD"))
    if not left or not right or left == right or left in ("MS", "MI") or right in ("MS", "MI"):
        raise ValueError("Nomes dos motores devem ser distintos e não podem ser MS/MI auxiliares")
    world.motors = {left: Motor("left", -1 if config.get("invert_left", False) else 1),
                    right: Motor("right", -1 if config.get("invert_right", True) else 1),
                    "MS": Motor(), "MI": Motor()}
    return world


class Session:
    def __init__(self, config=None):
        self.world = build_world(config or {})
        self.lock = threading.RLock()
        self.stop_event = threading.Event()
        self.process = None
        self.started = False
        self.clock = 0.0
        self.status = "pronto"
        self.logs = ""
        self.telemetry = ""
        self.gamepads = [[0] * 20, [0] * 20]
        self.servos = {"servo": .5}
        self.limit = float((config or {}).get("duration", 30))
        if not math.isfinite(self.limit) or not 1 <= self.limit <= 180:
            raise ValueError("Duração deve ser de 1 a 180 segundos")
        self.worker = None

    def log(self, message):
        with self.lock:
            self.logs = (self.logs + str(message))[-24000:]

    def stop(self):
        self.stop_event.set()
        with self.lock:
            for motor in self.world.motors.values():
                motor.power = 0
            if self.status not in ("concluído", "erro"):
                self.status = "parado"
            process = self.process
        if process and process.poll() is None:
            try:
                process.terminate()
            except OSError:
                pass

    def snapshot(self):
        with self.lock:
            result = self.world.snapshot()
            result.update(status=self.status, logs=self.logs, telemetry=self.telemetry, servos=self.servos.copy())
            return result

    def tick(self):
        next_tick = time.monotonic()
        while not self.stop_event.is_set():
            next_tick += .01
            with self.lock:
                self.clock += .01
                if self.started and self.status == "executando":
                    self.world.step(.01)
                    if self.world.elapsed >= self.limit:
                        self.log("\nLimite de tempo atingido.\n")
                        self.stop()
            self.stop_event.wait(max(0, next_tick-time.monotonic()))
            # Evita acumular passos depois de suspensão do computador.
            if time.monotonic() - next_tick > .1:
                next_tick = time.monotonic()

    def rpc(self, op, *args):
        if op == "idle":
            self.stop_event.wait(.01)
            return ""
        with self.lock:
            w = self.world
            if op == "time": return str(self.clock)
            if op == "active": return str(self.started and not self.stop_event.is_set()).lower()
            if op == "started": return str(self.started).lower()
            if op == "stopped": return str(self.stop_event.is_set()).lower()
            if op in ("stop", "finish"):
                self.stop_event.set()
                self.status = "concluído" if op == "finish" else "parado"
                for m in w.motors.values(): m.power = 0
                return ""
            if op == "device":
                kind, name = args
                valid = (kind in ("DcMotor", "DcMotorEx") and name in w.motors) or (kind == "IMU" and name == "imu") or (kind == "Servo" and name in self.servos)
                if not valid: raise ValueError(f"Hardware não configurado: {kind} '{name}'")
                return ""
            if op == "gamepad":
                return ",".join(str(x) for x in self.gamepads[int(args[0])-1])
            if op == "telemetry":
                self.telemetry = args[0][-12000:]
                return ""
            if op == "yaw": return str(math.degrees((w.yaw-w.yaw_zero+math.pi) % (2*math.pi)-math.pi))
            if op == "resetYaw":
                w.yaw_zero = w.yaw
                return ""
            if op == "servo":
                name, action, *values = args
                if action == "get": return str(self.servos[name])
                position = float(values[0])
                if not math.isfinite(position): raise ValueError("Posição de servo inválida")
                self.servos[name] = max(0, min(1, position))
                return ""
            if op == "motor":
                name, action, *values = args
                m = w.motors[name]
                getters = {"getPower": m.power, "getDirection": "FORWARD" if m.direction == 1 else "REVERSE",
                           "getMode": m.mode, "encoder": m.position(), "getTarget": m.target, "getBrake": m.brake,
                           "busy": str(m.mode == "RUN_TO_POSITION" and abs(m.target-m.position()) > 2).lower()}
                if action in getters: return str(getters[action])
                if action == "power":
                    power = float(values[0])
                    if not math.isfinite(power): raise ValueError("Potência não finita")
                    m.power = max(-1, min(1, power))
                elif action == "direction": m.direction = 1 if values[0] == "FORWARD" else -1
                elif action == "mode":
                    m.mode = values[0]
                    if m.mode == "STOP_AND_RESET_ENCODER":
                        m.zero = m.raw_ticks
                        m.power = 0
                        m.target = 0
                elif action == "target": m.target = int(values[0])
                elif action == "brake": m.brake = values[0]
                else: raise ValueError("Operação de motor não implementada: " + action)
                return ""
            raise ValueError("API não implementada: " + op)

    def run(self, source):
        self.status = "compilando"
        self.worker = threading.Thread(target=self._execute, args=(source,), daemon=True)
        self.worker.start()

    def _execute(self, source):
        ticker = None
        watchdog = None
        try:
            compiler, java = executable("javac"), executable("java")
            if not compiler or not java:
                raise ValueError("Instale JDK 17 ou superior; configure JAVA_HOME ou PATH. Java sem javac não compila código.")
            stripped = re.sub(r"/\*.*?\*/|//[^\n]*", "", source, flags=re.S)
            match = re.search(r"\bpublic\s+class\s+([A-Za-z_$][\w$]*)\s+extends\s+LinearOpMode\b", stripped)
            package = re.search(r"\bpackage\s+([\w.]+)\s*;", stripped)
            if not match:
                raise ValueError("Cole uma classe pública que estenda LinearOpMode, com imports e runOpMode().")
            classname = match.group(1)
            qualified = (package.group(1) + "." if package else "") + classname
            with tempfile.TemporaryDirectory(prefix="meca-sim-") as directory:
                files = write_sdk(directory)
                user = Path(directory) / (classname + ".java")
                user.write_text(source, encoding="utf-8")
                classes = Path(directory) / "classes"
                result = subprocess.run([compiler, "-encoding", "UTF-8", "-d", str(classes), *files, str(user)],
                                        capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=30)
                self.log(result.stdout + result.stderr)
                if result.returncode:
                    raise ValueError("Compilação falhou. Veja erros acima; APIs fora da camada suportada precisam ser implementadas.")
                if self.stop_event.is_set(): return
                with self.lock:
                    self.status = "inicializando"
                    self.process = subprocess.Popen([java, "-Xmx128m", "-cp", str(classes), "sim.Bridge", qualified],
                                                    stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                                    text=True, encoding="utf-8", errors="replace", bufsize=1)
                watchdog = threading.Timer(190, self.stop)
                watchdog.daemon = True
                watchdog.start()
                ticker = threading.Thread(target=self.tick, daemon=True)
                ticker.start()
                for line in self.process.stdout:
                    if line.startswith("@SIM@"):
                        args = [base64.b64decode(s).decode("utf-8") for s in line[5:].strip().split("|")]
                        try:
                            answer = self.rpc(*args)
                        except (ValueError, KeyError, IndexError) as error:
                            answer = "ERROR:" + str(error)
                        self.process.stdin.write(base64.b64encode(answer.encode()).decode() + "\n")
                        self.process.stdin.flush()
                    else:
                        self.log(line)
                code = self.process.wait(timeout=2)
                if code and self.status != "parado":
                    raise ValueError(f"Execução Java terminou com código {code}")
                if self.status not in ("parado", "erro"):
                    self.status = "concluído"
        except Exception as error:
            if self.status != "parado":
                self.status = "erro"
                self.log("\n" + str(error) + "\n")
        finally:
            if watchdog: watchdog.cancel()
            self.stop_event.set()
            with self.lock:
                for m in self.world.motors.values(): m.power = 0
            if self.process:
                if self.process.poll() is None:
                    self.process.kill()
                self.process.wait()
                self.process.stdout.close()
                self.process.stdin.close()
            if ticker: ticker.join(timeout=1)


class App:
    def __init__(self):
        self.session = Session()
        self.lock = threading.RLock()
        self.token = secrets.token_urlsafe(32)


def handler_for(app):
    class Handler(BaseHTTPRequestHandler):
        def log_message(self, *args): pass

        def send(self, data, status=200, mime="application/json; charset=utf-8"):
            body = json.dumps(data, ensure_ascii=False).encode() if mime.startswith("application/json") else data
            self.send_response(status)
            self.send_header("Content-Type", mime)
            self.send_header("Content-Length", str(len(body)))
            self.send_header("Cache-Control", "no-store")
            self.send_header("X-Content-Type-Options", "nosniff")
            self.end_headers()
            self.wfile.write(body)

        def allowed_host(self):
            host = self.headers.get("Host", "").split(":")[0]
            return host in ("127.0.0.1", "localhost")

        def do_GET(self):
            if not self.allowed_host(): return self.send({"error": "Host inválido"}, 403)
            path = urlparse(self.path).path
            if path == "/api/state": return self.send(app.session.snapshot())
            files = {"/": ("web/index.html", "text/html; charset=utf-8"),
                     "/app.js": ("web/app.js", "text/javascript; charset=utf-8"),
                     "/model.mjs": ("web/model.mjs", "text/javascript; charset=utf-8"),
                     "/style.css": ("web/style.css", "text/css; charset=utf-8"),
                     "/examples": ("examples.json", "application/json; charset=utf-8")}
            if path not in files: return self.send({"error": "Não encontrado"}, 404)
            name, mime = files[path]
            body = (ROOT / name).read_bytes()
            if path == "/": body = body.replace(b"__TOKEN__", app.token.encode())
            self.send(body, mime=mime if not mime.startswith("application/json") else "text/plain; charset=utf-8")

        def do_POST(self):
            origin = self.headers.get("Origin")
            if not self.allowed_host() or self.headers.get("X-Sim-Token") != app.token:
                return self.send({"error": "Acesso recusado"}, 403)
            if origin and origin != "http://" + self.headers.get("Host", ""):
                return self.send({"error": "Origem inválida"}, 403)
            try:
                length = int(self.headers.get("Content-Length", 0))
                if not 0 < length <= 250000: raise ValueError("Conteúdo ausente ou grande demais")
                data = json.loads(self.rfile.read(length))
                with app.lock:
                    session = app.session
                    if self.path == "/api/run":
                        if session.worker and session.worker.is_alive():
                            return self.send({"error": "Pare a execução atual antes de compilar outra"}, 409)
                        source = data.get("source", "")
                        if not isinstance(source, str): raise ValueError("Código inválido")
                        app.session = Session(data.get("config", {}))
                        app.session.run(source)
                    elif self.path == "/api/start":
                        with session.lock:
                            if session.status != "inicializando": raise ValueError("Aguarde a inicialização Java")
                            session.started = True
                            session.status = "executando"
                    elif self.path == "/api/stop": session.stop()
                    elif self.path == "/api/reset":
                        if session.worker and session.worker.is_alive():
                            return self.send({"error": "Pare e aguarde o Java encerrar antes de reiniciar"}, 409)
                        app.session = Session(data.get("config", {}))
                    elif self.path == "/api/gamepad":
                        with session.lock:
                            for index in range(2):
                                values = data.get(str(index+1), [0]*20)
                                if len(values) != 20: raise ValueError("Gamepad inválido")
                                values = [float(v) for v in values]
                                if not all(math.isfinite(v) for v in values): raise ValueError("Gamepad inválido")
                                session.gamepads[index] = [max(-1, min(1,v)) for v in values[:4]] + [max(0,min(1,v)) for v in values[4:6]] + [int(bool(v)) for v in values[6:]]
                    else: return self.send({"error": "Não encontrado"}, 404)
                self.send({"ok": True})
            except (ValueError, TypeError, KeyError) as error:
                self.send({"error": str(error)}, 400)
    return Handler


def main():
    app = App()
    port = int(os.environ.get("SIM_PORT", "8765"))
    server = ThreadingHTTPServer(("127.0.0.1", port), handler_for(app))
    print(f"Meca Sim • abra http://127.0.0.1:{port}", flush=True)
    print("Java colado executa com as permissões da sua conta. Execute somente código confiável.", flush=True)
    try: server.serve_forever()
    except KeyboardInterrupt: pass
    finally:
        app.session.stop()
        if app.session.worker: app.session.worker.join(timeout=3)
        server.server_close()


if __name__ == "__main__": main()
