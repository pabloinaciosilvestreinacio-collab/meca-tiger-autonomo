package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@Autonomous(name = "Simulador: encoder e IMU")
public class AutonomoDemo extends LinearOpMode {
    private DcMotor ME, MD;
    private IMU imu;

    @Override
    public void runOpMode() throws InterruptedException {
        ME = hardwareMap.get(DcMotor.class, "ME");
        MD = hardwareMap.get(DcMotor.class, "MD");
        imu = hardwareMap.get(IMU.class, "imu");
        ME.setDirection(DcMotor.Direction.FORWARD);
        MD.setDirection(DcMotor.Direction.REVERSE);
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
            RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
            RevHubOrientationOnRobot.UsbFacingDirection.UP)));
        telemetry.addLine("Pronto: START para avançar 1 m, girar 50 graus e avançar 0,6 m.");
        telemetry.update();
        waitForStart();
        imu.resetYaw();
        try {
            andar(1.0);
            girar(50);
            andar(0.6);
        } finally {
            ME.setPower(0);
            MD.setPower(0);
        }
    }

    private void andar(double metros) {
        ME.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        MD.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ME.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        MD.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < 4) {
            double distancia = (ME.getCurrentPosition() + MD.getCurrentPosition()) / (2 * 1980.6);
            if (distancia >= metros) break;
            ME.setPower(0.5);
            MD.setPower(0.5);
            telemetry.addData("Distância (m)", distancia);
            telemetry.update();
            idle();
        }
        ME.setPower(0);
        MD.setPower(0);
    }

    private void girar(double alvo) {
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < 3) {
            double yaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double erro = AngleUnit.DEGREES.normalize(alvo - yaw);
            if (Math.abs(erro) < 1) break;
            double correcao = Range.clip(erro * 0.015, -0.35, 0.35);
            ME.setPower(-correcao);
            MD.setPower(correcao);
            telemetry.addData("Yaw (graus)", yaw);
            telemetry.update();
            idle();
        }
        ME.setPower(0);
        MD.setPower(0);
    }
}
