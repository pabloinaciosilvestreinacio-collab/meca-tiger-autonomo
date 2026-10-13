"""Camada de compatibilidade FTC parcial. Não é o SDK oficial."""
from pathlib import Path

SOURCES = {
"sim/Bridge.java": r'''package sim;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
public final class Bridge {
    private static final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    public static synchronized String call(String op, Object... args) {
        StringBuilder line = new StringBuilder("@SIM@");
        line.append(enc(op));
        for (Object arg : args) line.append("|").append(enc(String.valueOf(arg)));
        System.out.println(line); System.out.flush();
        try {
            String answer = input.readLine();
            if (answer == null) throw new IllegalStateException("Simulação encerrada");
            answer = new String(Base64.getDecoder().decode(answer), StandardCharsets.UTF_8);
            if (answer.startsWith("ERROR:")) throw new IllegalArgumentException(answer.substring(6));
            return answer;
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    private static String enc(String s) { return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8)); }
    public static double number(String op, Object... args) { return Double.parseDouble(call(op, args)); }
    public static void main(String[] args) throws Exception {
        com.qualcomm.robotcore.eventloop.opmode.LinearOpMode mode =
            (com.qualcomm.robotcore.eventloop.opmode.LinearOpMode) Class.forName(args[0]).getDeclaredConstructor().newInstance();
        try { mode.runOpMode(); } finally { call("finish"); }
    }
}''',
"com/qualcomm/robotcore/eventloop/opmode/LinearOpMode.java": r'''package com.qualcomm.robotcore.eventloop.opmode;
import sim.Bridge;
import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.Telemetry;
public abstract class LinearOpMode {
    public final HardwareMap hardwareMap = new HardwareMap();
    public final Gamepad gamepad1 = new Gamepad(1), gamepad2 = new Gamepad(2);
    public final Telemetry telemetry = new Telemetry();
    private double runtimeZero;
    public abstract void runOpMode() throws InterruptedException;
    public void waitForStart() {
        while (!isStarted() && !isStopRequested()) idle();
        resetRuntime();
    }
    public boolean opModeIsActive() { gamepad1.refresh(); gamepad2.refresh(); return Bridge.call("active").equals("true"); }
    public boolean opModeInInit() { return !isStarted() && !isStopRequested(); }
    public boolean isStarted() { return Bridge.call("started").equals("true"); }
    public boolean isStopRequested() { return Bridge.call("stopped").equals("true"); }
    public void requestOpModeStop() { Bridge.call("stop"); }
    public void idle() { Bridge.call("idle"); gamepad1.refresh(); gamepad2.refresh(); }
    public void sleep(long milliseconds) { double end = Bridge.number("time") + milliseconds/1000.0;
        while (!isStopRequested() && Bridge.number("time") < end) idle(); }
    public void resetRuntime() { runtimeZero = Bridge.number("time"); }
    public double getRuntime() { return Bridge.number("time") - runtimeZero; }
}''',
"com/qualcomm/robotcore/hardware/DcMotorSimple.java": r'''package com.qualcomm.robotcore.hardware;
public interface DcMotorSimple {
    enum Direction { FORWARD, REVERSE }
    void setDirection(Direction direction);
    Direction getDirection();
    void setPower(double power);
    double getPower();
}''',
"com/qualcomm/robotcore/hardware/DcMotor.java": r'''package com.qualcomm.robotcore.hardware;
import sim.Bridge;
public class DcMotor implements DcMotorSimple {
    public enum RunMode { RUN_WITHOUT_ENCODER, RUN_USING_ENCODER, RUN_TO_POSITION, STOP_AND_RESET_ENCODER }
    public enum ZeroPowerBehavior { BRAKE, FLOAT }
    protected final String name;
    public DcMotor(String name) { this.name=name; }
    public void setPower(double power) { Bridge.call("motor",name,"power",power); }
    public double getPower() { return Bridge.number("motor",name,"getPower"); }
    public void setDirection(Direction direction) { Bridge.call("motor",name,"direction",direction.name()); }
    public Direction getDirection() { return Direction.valueOf(Bridge.call("motor",name,"getDirection")); }
    public void setMode(RunMode mode) { Bridge.call("motor",name,"mode",mode.name()); }
    public RunMode getMode() { return RunMode.valueOf(Bridge.call("motor",name,"getMode")); }
    public int getCurrentPosition() { return (int) Bridge.number("motor",name,"encoder"); }
    public void setTargetPosition(int target) { Bridge.call("motor",name,"target",target); }
    public int getTargetPosition() { return (int) Bridge.number("motor",name,"getTarget"); }
    public boolean isBusy() { return Bridge.call("motor",name,"busy").equals("true"); }
    public void setZeroPowerBehavior(ZeroPowerBehavior behavior) { Bridge.call("motor",name,"brake",behavior.name()); }
    public ZeroPowerBehavior getZeroPowerBehavior() { return ZeroPowerBehavior.valueOf(Bridge.call("motor",name,"getBrake")); }
}''',
"com/qualcomm/robotcore/hardware/DcMotorEx.java": r'''package com.qualcomm.robotcore.hardware;
public class DcMotorEx extends DcMotor { public DcMotorEx(String name) { super(name); } }''',
"com/qualcomm/robotcore/hardware/HardwareMap.java": r'''package com.qualcomm.robotcore.hardware;
import sim.Bridge;
public class HardwareMap {
    public final DeviceMapping<DcMotor> dcMotor = new DeviceMapping<>(DcMotor.class);
    public final DeviceMapping<Servo> servo = new DeviceMapping<>(Servo.class);
    public <T> T get(Class<? extends T> type, String name) {
        Bridge.call("device",type.getSimpleName(),name);
        Object result;
        if (type==DcMotor.class) result=new DcMotor(name);
        else if (type==DcMotorEx.class) result=new DcMotorEx(name);
        else if (type==Servo.class) result=new Servo(name);
        else if (type==IMU.class) result=new IMU(name);
        else throw new IllegalArgumentException("Hardware não implementado: "+type.getName());
        return type.cast(result);
    }
    public class DeviceMapping<T> {
        private final Class<T> type;
        public DeviceMapping(Class<T> type) { this.type=type; }
        public T get(String name) { return HardwareMap.this.get(type,name); }
    }
}''',
"com/qualcomm/robotcore/hardware/Gamepad.java": r'''package com.qualcomm.robotcore.hardware;
import sim.Bridge;
public class Gamepad {
    public float left_stick_x,left_stick_y,right_stick_x,right_stick_y,left_trigger,right_trigger;
    public boolean a,b,x,y,left_bumper,right_bumper,back,start,left_stick_button,right_stick_button;
    public boolean dpad_up,dpad_down,dpad_left,dpad_right;
    private final int index;
    public Gamepad(int index) { this.index=index; }
    public void refresh() {
        String[] v=Bridge.call("gamepad",index).split(",");
        left_stick_x=Float.parseFloat(v[0]); left_stick_y=Float.parseFloat(v[1]);
        right_stick_x=Float.parseFloat(v[2]); right_stick_y=Float.parseFloat(v[3]);
        left_trigger=Float.parseFloat(v[4]); right_trigger=Float.parseFloat(v[5]);
        boolean[] buttons=new boolean[14]; for(int i=0;i<14;i++) buttons[i]=v[i+6].equals("1");
        a=buttons[0]; b=buttons[1]; x=buttons[2]; y=buttons[3]; left_bumper=buttons[4]; right_bumper=buttons[5];
        back=buttons[6]; start=buttons[7]; left_stick_button=buttons[8]; right_stick_button=buttons[9];
        dpad_up=buttons[10]; dpad_down=buttons[11]; dpad_left=buttons[12]; dpad_right=buttons[13];
    }
}''',
"com/qualcomm/robotcore/hardware/Servo.java": r'''package com.qualcomm.robotcore.hardware;
import sim.Bridge;
public class Servo {
    public enum Direction { FORWARD, REVERSE }
    private final String name;
    private Direction direction=Direction.FORWARD;
    public Servo(String name) { this.name=name; }
    public void setPosition(double position) { Bridge.call("servo",name,"set",position,direction.name()); }
    public double getPosition() { return Bridge.number("servo",name,"get"); }
    public void setDirection(Direction direction) { this.direction=direction; }
    public Direction getDirection() { return direction; }
}''',
"com/qualcomm/robotcore/hardware/IMU.java": r'''package com.qualcomm.robotcore.hardware;
import sim.Bridge;
import org.firstinspires.ftc.robotcore.external.navigation.*;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
public class IMU {
    public static class Parameters { public Parameters(RevHubOrientationOnRobot orientation) {} }
    public IMU(String name) {}
    public boolean initialize(Parameters parameters) { return true; }
    public void resetYaw() { Bridge.call("resetYaw"); }
    public YawPitchRollAngles getRobotYawPitchRollAngles() {
        return new YawPitchRollAngles(AngleUnit.DEGREES,Bridge.number("yaw"),0,0,(long)(Bridge.number("time")*1e9));
    }
}''',
"org/firstinspires/ftc/robotcore/external/navigation/AngleUnit.java": r'''package org.firstinspires.ftc.robotcore.external.navigation;
public enum AngleUnit {
    DEGREES, RADIANS;
    public double fromDegrees(double value) { return this==DEGREES?value:Math.toRadians(value); }
    public double fromRadians(double value) { return this==RADIANS?value:Math.toDegrees(value); }
    public double normalize(double value) { double turn=this==DEGREES?360:2*Math.PI; return ((value+turn/2)%turn+turn)%turn-turn/2; }
}''',
"org/firstinspires/ftc/robotcore/external/navigation/YawPitchRollAngles.java": r'''package org.firstinspires.ftc.robotcore.external.navigation;
public class YawPitchRollAngles {
    private final double yaw,pitch,roll;
    private final long time;
    public YawPitchRollAngles(AngleUnit unit,double yaw,double pitch,double roll,long time) {
        this.yaw=unit==AngleUnit.DEGREES?yaw:Math.toDegrees(yaw);
        this.pitch=unit==AngleUnit.DEGREES?pitch:Math.toDegrees(pitch);
        this.roll=unit==AngleUnit.DEGREES?roll:Math.toDegrees(roll); this.time=time;
    }
    public double getYaw(AngleUnit unit) { return unit.fromDegrees(yaw); }
    public double getPitch(AngleUnit unit) { return unit.fromDegrees(pitch); }
    public double getRoll(AngleUnit unit) { return unit.fromDegrees(roll); }
    public long getAcquisitionTime() { return time; }
}''',
"com/qualcomm/hardware/rev/RevHubOrientationOnRobot.java": r'''package com.qualcomm.hardware.rev;
public class RevHubOrientationOnRobot {
    public enum LogoFacingDirection { UP, DOWN, FORWARD, BACKWARD, LEFT, RIGHT }
    public enum UsbFacingDirection { UP, DOWN, FORWARD, BACKWARD, LEFT, RIGHT }
    public RevHubOrientationOnRobot(LogoFacingDirection logo,UsbFacingDirection usb) {}
}''',
"com/qualcomm/robotcore/util/ElapsedTime.java": r'''package com.qualcomm.robotcore.util;
import sim.Bridge;
public class ElapsedTime {
    public enum Resolution { SECONDS, MILLISECONDS }
    private double start;
    private final Resolution resolution;
    public ElapsedTime() { this(Resolution.SECONDS); }
    public ElapsedTime(Resolution resolution) { this.resolution=resolution; reset(); }
    public void reset() { start=Bridge.number("time"); }
    public double seconds() { return Bridge.number("time")-start; }
    public double milliseconds() { return seconds()*1000; }
    public double time() { return resolution==Resolution.SECONDS?seconds():milliseconds(); }
}''',
"com/qualcomm/robotcore/util/Range.java": r'''package com.qualcomm.robotcore.util;
public class Range { public static double clip(double value,double min,double max) { return Math.max(min,Math.min(max,value)); } }''',
"org/firstinspires/ftc/robotcore/external/Telemetry.java": r'''package org.firstinspires.ftc.robotcore.external;
import sim.Bridge;
import java.util.LinkedHashMap;
public class Telemetry {
    public static class Item {}
    public static class Line {}
    private final LinkedHashMap<String,String> data=new LinkedHashMap<>();
    public Item addData(String caption,Object value) { data.put(caption,String.valueOf(value)); return new Item(); }
    public Item addData(String caption,String format,Object... args) { return addData(caption,String.format(format,args)); }
    public Line addLine(String line) { data.put("linha"+data.size(),line); return new Line(); }
    public boolean update() {
        StringBuilder text=new StringBuilder();
        data.forEach((k,v)->text.append(k).append(": ").append(v).append("\n"));
        Bridge.call("telemetry",text.toString()); data.clear(); return true;
    }
    public void clear() { data.clear(); }
    public void clearAll() { clear(); }
    public void setMsTransmissionInterval(int milliseconds) {}
}''',
}

for annotation in ("Autonomous", "TeleOp", "Disabled"):
    SOURCES[f"com/qualcomm/robotcore/eventloop/opmode/{annotation}.java"] = f'''package com.qualcomm.robotcore.eventloop.opmode;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
public @interface {annotation} {{ String name() default ""; String group() default ""; }}'''


def write_sdk(directory):
    result = []
    for name, source in SOURCES.items():
        path = Path(directory) / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(source, encoding="utf-8")
        result.append(str(path))
    return result
