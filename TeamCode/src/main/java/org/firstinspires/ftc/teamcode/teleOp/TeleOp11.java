package org.firstinspires.ftc.teamcode.teleOp;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "TeleOp 1.1 - Manual Assistido", group = "Meca Tiger")
public class TeleOp11 extends LinearOpMode {

    private static final double DEAD_ZONE = 0.08;

    private static final double VELOCIDADE_PRECISAO = 0.35;
    private static final double VELOCIDADE_NORMAL = 0.70;
    private static final double VELOCIDADE_TURBO = 1.00;

    private static final double RAMPA_ACELERACAO = 2.0;
    private static final double RAMPA_FRENAGEM = 4.0;

    private boolean curvaQuadratica = true;
    private boolean startAnterior;
    private double potenciaAnteriorMD;
    private double potenciaAnteriorME;

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

            telemetry.addLine("TeleOp 1.1 - chassi sem IMU e mecanismos");
            telemetry.addLine("LB precisao 35% | Normal 70% | RB turbo 100%");
            telemetry.addLine("START alterna a curva quadratica");
            telemetry.update();

            waitForStart();
            if (isStopRequested()) {
                return;
            }

            ElapsedTime tempoLoop = new ElapsedTime();
            startAnterior = gamepad1.start;

            while (opModeIsActive()) {
                double dt = Range.clip(tempoLoop.seconds(), 0.0, 0.10);
                tempoLoop.reset();

                boolean startAtual = gamepad1.start;
                if (startAtual && !startAnterior) {
                    curvaQuadratica = !curvaQuadratica;
                }
                startAnterior = startAtual;

                double y = aplicarDeadZone(gamepad1.left_stick_y);
                double x = aplicarDeadZone(gamepad1.right_stick_x);

                if (curvaQuadratica) {
                    y = quadradoComSinal(y);
                    x = quadradoComSinal(x);
                }

                double potenciaMD = -x - y;
                double potenciaME = -x + y;

                double divisor = Math.max(1.0,
                        Math.max(Math.abs(potenciaMD), Math.abs(potenciaME)));
                potenciaMD /= divisor;
                potenciaME /= divisor;

                double velocidade = VELOCIDADE_NORMAL;
                if (gamepad1.left_bumper) {
                    velocidade = VELOCIDADE_PRECISAO;
                } else if (gamepad1.right_bumper) {
                    velocidade = VELOCIDADE_TURBO;
                }

                double alvoMD = Range.clip(potenciaMD * velocidade, -1.0, 1.0);
                double alvoME = Range.clip(potenciaME * velocidade, -1.0, 1.0);

                potenciaAnteriorMD = aplicarRampa(
                        potenciaAnteriorMD, alvoMD, dt);
                potenciaAnteriorME = aplicarRampa(
                        potenciaAnteriorME, alvoME, dt);

                MD.setPower(potenciaAnteriorMD);
                ME.setPower(potenciaAnteriorME);

                telemetry.addData("Modo", gamepad1.left_bumper ? "Precisao"
                        : gamepad1.right_bumper ? "Turbo" : "Normal");
                telemetry.addData("Curva quadratica", curvaQuadratica);
                telemetry.addData("Potencia MD", "%.3f", potenciaAnteriorMD);
                telemetry.addData("Potencia ME", "%.3f", potenciaAnteriorME);
                telemetry.addData("Tempo loop", "%.3f s", dt);
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

    private static double quadradoComSinal(double valor) {
        return Math.copySign(valor * valor, valor);
    }

    private static double aplicarRampa(
            double atual, double alvo, double dt) {
        boolean invertendo = atual != 0.0 && alvo != 0.0
                && Math.signum(atual) != Math.signum(alvo);
        double destino = invertendo ? 0.0 : alvo;

        boolean acelerando = (atual == 0.0 && destino != 0.0)
                || (Math.signum(atual) == Math.signum(destino)
                        && Math.abs(destino) > Math.abs(atual));
        double taxa = acelerando
                ? RAMPA_ACELERACAO : RAMPA_FRENAGEM;

        double passo = taxa * Range.clip(dt, 0.0, 0.10);
        return Range.clip(destino, atual - passo, atual + passo);
    }
}
