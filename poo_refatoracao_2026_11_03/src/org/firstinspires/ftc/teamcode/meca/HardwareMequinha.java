package org.firstinspires.ftc.teamcode.meca;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

/** Ligacao unica dos nomes de hardware e dos estados seguros dos atuadores.
 *  Nesta primeira etapa nao implementa movimento, coleta ou tiros automaticos.
 */
public final class HardwareMequinha {
    private final DcMotor me;
    private final DcMotor md;
    private final DcMotor mi;
    private final DcMotor ms;
    private final Servo sa;
    private final IMU imu;

    public HardwareMequinha(HardwareMap mapa) {
        me = mapa.get(DcMotor.class, "ME");
        md = mapa.get(DcMotor.class, "MD");
        mi = mapa.get(DcMotor.class, "MI");
        ms = mapa.get(DcMotor.class, "MS");
        sa = mapa.get(Servo.class, "SA");
        imu = mapa.get(IMU.class, "imu");

        me.setDirection(DcMotor.Direction.FORWARD);
        md.setDirection(DcMotor.Direction.REVERSE);
        mi.setDirection(DcMotor.Direction.FORWARD);
        ms.setDirection(DcMotor.Direction.FORWARD);
        me.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        md.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        me.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        md.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        mi.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        ms.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        pararTudo();
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.UP)));
    }

    public DcMotor esquerda() { return me; }
    public DcMotor direita() { return md; }
    public DcMotor intake() { return mi; }
    public DcMotor shooter() { return ms; }
    public Servo alimentador() { return sa; }
    public IMU imu() { return imu; }

    public void pararTracao() {
        me.setPower(0.0);
        md.setPower(0.0);
    }

    public void pararMecanismos() {
        mi.setPower(0.0);
        ms.setPower(0.0);
        sa.setPosition(ParametrosMequinha.SERVO_FECHADO);
    }

    public void pararTudo() {
        pararTracao();
        pararMecanismos();
    }
}
