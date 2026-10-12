package org.firstinspires.ftc.teamcode.Autonomo;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "Diagnostico sinais MD ME IMU", group = "Teste")
public class DiagnosticoSinais extends LinearOpMode {

    @Override
    public void runOpMode() {
        DcMotor direito = hardwareMap.get(DcMotor.class, "MD");
        DcMotor esquerdo = hardwareMap.get(DcMotor.class, "ME");
        IMU imu = hardwareMap.get(IMU.class, "imu");

        direito.setPower(0);
        esquerdo.setPower(0);
        direito.setDirection(DcMotor.Direction.REVERSE);
        esquerdo.setDirection(DcMotor.Direction.FORWARD);
        direito.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        esquerdo.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        boolean imuOk = imu.initialize(new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP)));

        int inicioDireito = direito.getCurrentPosition();
        int inicioEsquerdo = esquerdo.getCurrentPosition();

        telemetry.addData("IMU", imuOk);
        telemetry.addLine("SEM ENERGIZAR MOTORES");
        telemetry.addLine("Com robô suspenso, gire as rodas a mao.");
        telemetry.addLine("Gire o robo manualmente para conferir yaw.");
        telemetry.update();

        try {
            waitForStart();
            if (isStopRequested() || !imuOk) return;

            imu.resetYaw();

            while (opModeIsActive()) {
                direito.setPower(0);
                esquerdo.setPower(0);

                double yaw = imu.getRobotYawPitchRollAngles()
                        .getYaw(AngleUnit.DEGREES);

                telemetry.addData("Yaw em graus (CCW positivo)", "%.2f", yaw);
                telemetry.addData("Ticks MD relativos",
                        direito.getCurrentPosition() - inicioDireito);
                telemetry.addData("Ticks ME relativos",
                        esquerdo.getCurrentPosition() - inicioEsquerdo);
                telemetry.addLine("Avanco manual: ambos ticks devem aumentar.");
                telemetry.addLine("Giro anti-horario manual: yaw deve aumentar.");
                telemetry.addLine("Se divergirem, NAO executar o AUTO integrado.");
                telemetry.update();
                sleep(100);
            }
        } finally {
            direito.setPower(0);
            esquerdo.setPower(0);
            direito.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            esquerdo.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }
}
