package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Simulador: controle diferencial")
public class TeleOpDemo extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor ME = hardwareMap.get(DcMotor.class, "ME");
        DcMotor MD = hardwareMap.get(DcMotor.class, "MD");
        ME.setDirection(DcMotor.Direction.FORWARD);
        MD.setDirection(DcMotor.Direction.REVERSE);
        waitForStart();
        try {
            while (opModeIsActive()) {
                double frente = -gamepad1.left_stick_y;
                double giro = -gamepad1.right_stick_x;
                double esquerda = frente - giro;
                double direita = frente + giro;
                double escala = Math.max(1, Math.max(Math.abs(esquerda), Math.abs(direita)));
                ME.setPower(esquerda / escala);
                MD.setPower(direita / escala);
                telemetry.addData("ME", ME.getCurrentPosition());
                telemetry.addData("MD", MD.getCurrentPosition());
                telemetry.update();
                idle();
            }
        } finally {
            ME.setPower(0);
            MD.setPower(0);
        }
    }
}
