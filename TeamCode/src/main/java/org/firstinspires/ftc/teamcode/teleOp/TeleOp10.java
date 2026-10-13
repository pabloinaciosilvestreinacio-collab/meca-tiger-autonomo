package org.firstinspires.ftc.teamcode.teleOp;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "TeleOp 1.0 - Chassi", group = "Meca Tiger")
public class TeleOp10 extends LinearOpMode {

    private static final double DEAD_ZONE = 0.08;
    private static final double VELOCIDADE_TESTE = 0.35;

    @Override
    public void runOpMode() {
        DcMotor MD = hardwareMap.get(DcMotor.class, "MD");
        DcMotor ME = hardwareMap.get(DcMotor.class, "ME");

        try {
            MD.setPower(0);
            ME.setPower(0);

            MD.setDirection(DcMotor.Direction.REVERSE);
            ME.setDirection(DcMotor.Direction.FORWARD);

            MD.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            ME.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

            MD.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            ME.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

            telemetry.addLine("TeleOp 1.0 - somente chassi");
            telemetry.addLine("Joystick esquerdo Y: frente/tras");
            telemetry.addLine("Joystick direito X: giro");
            telemetry.addLine("MI e MS: nao controlados por este OpMode");
            telemetry.update();

            waitForStart();

            while (opModeIsActive()) {
                double y = aplicarDeadZone(gamepad1.left_stick_y);
                double x = aplicarDeadZone(gamepad1.right_stick_x);

                double potenciaMD = Range.clip(-x - y, -1.0, 1.0);
                double potenciaME = Range.clip(-x + y, -1.0, 1.0);

                MD.setPower(potenciaMD * VELOCIDADE_TESTE);
                ME.setPower(potenciaME * VELOCIDADE_TESTE);

                telemetry.addData("Potencia MD", "%.3f", MD.getPower());
                telemetry.addData("Potencia ME", "%.3f", ME.getPower());
                telemetry.addLine("Sinais da tracao ainda dependem de teste fisico");
                telemetry.update();
                idle();
            }
        } finally {
            MD.setPower(0);
            ME.setPower(0);
        }
    }

    private static double aplicarDeadZone(double valor) {
        if (!Double.isFinite(valor) || Math.abs(valor) <= DEAD_ZONE) {
            return 0.0;
        }
        return Range.clip(valor, -1.0, 1.0);
    }
}
