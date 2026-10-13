package org.firstinspires.ftc.teamcode.teleOp;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "TeleOp 1.2 - IMU Experimental", group = "Meca Tiger")
public class TeleOp12 extends LinearOpMode {

    private static final double DEAD_ZONE = 0.08;
    private static final double VELOCIDADE_PRECISAO = 0.35;
    private static final double VELOCIDADE_NORMAL = 0.70;
    private static final double VELOCIDADE_TURBO = 1.00;
    private static final double RAMPA_ACELERACAO = 2.0;
    private static final double RAMPA_FRENAGEM = 4.0;
    private static final double KP_RUMO = 0.012;
    private static final double MAX_CORRECAO_X = 0.15;
    private static final double LIMITE_ERRO_RUMO = 12.0;

    private boolean curvaQuadratica = true;
    private boolean startAnterior;
    private boolean assistenciaAtiva;
    private boolean assistenciaBloqueada;
    private boolean aAnterior;
    private boolean sinalConfirmado;
    private int sinalYawPorX;
    private double rumoAlvo;
    private double potenciaAnteriorMD;
    private double potenciaAnteriorME;

    @Override
    public void runOpMode() {
        DcMotor MD = hardwareMap.get(DcMotor.class, "MD");
        DcMotor ME = hardwareMap.get(DcMotor.class, "ME");
        IMU imu = null;
        boolean imuDisponivel = false;

        try {
            MD.setPower(0);
            ME.setPower(0);
            MD.setDirection(DcMotor.Direction.REVERSE);
            ME.setDirection(DcMotor.Direction.FORWARD);
            MD.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            ME.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            MD.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            ME.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

            try {
                imu = hardwareMap.get(IMU.class, "imu");
                imuDisponivel = imu.initialize(new IMU.Parameters(
                        new RevHubOrientationOnRobot(
                                RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                                RevHubOrientationOnRobot.UsbFacingDirection.UP)));
            } catch (RuntimeException ex) {
                imuDisponivel = false;
            }

            while (!isStarted() && !isStopRequested()) {
                int anterior = sinalYawPorX;
                if (gamepad1.dpad_right) {
                    sinalYawPorX = +1;
                } else if (gamepad1.dpad_left) {
                    sinalYawPorX = -1;
                }
                boolean mudou = anterior != sinalYawPorX;
                if (mudou) {
                    sinalConfirmado = false;
                }
                if (!mudou && sinalYawPorX != 0
                        && gamepad1.a && !aAnterior) {
                    sinalConfirmado = true;
                }
                aAnterior = gamepad1.a;
                telemetry.addLine("TELEOP 1.2: IMU EXPERIMENTAL");
                telemetry.addLine("Dpad DIR: X positivo aumenta yaw");
                telemetry.addLine("Dpad ESQ: X positivo diminui yaw");
                telemetry.addLine("A confirma APOS selecionar sinal");
                telemetry.addData("IMU inicializado", imuDisponivel);
                telemetry.addData("Sinal escolhido", sinalYawPorX);
                telemetry.addData("Sinal confirmado", sinalConfirmado);
                telemetry.addLine("Sem confirmacao o chassi e 100% manual");
                telemetry.update();
                sleep(70);
            }
            waitForStart();
            if (isStopRequested()) {
                return;
            }
            if (imuDisponivel) {
                try {
                    imu.resetYaw();
                } catch (RuntimeException ex) {
                    imuDisponivel = false;
                }
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

                double yBruto = aplicarDeadZone(gamepad1.left_stick_y);
                double xBruto = aplicarDeadZone(gamepad1.right_stick_x);
                double y = curvaQuadratica ? quadradoComSinal(yBruto) : yBruto;
                double x = curvaQuadratica ? quadradoComSinal(xBruto) : xBruto;

                double yaw = Double.NaN;
                if (imuDisponivel) {
                    try {
                        yaw = imu.getRobotYawPitchRollAngles()
                                .getYaw(AngleUnit.DEGREES);
                    } catch (RuntimeException ex) {
                        imuDisponivel = false;
                    }
                }
                boolean leituraValida = Double.isFinite(yaw);
                boolean segurandoBack = gamepad1.back;
                if (!segurandoBack) {
                    assistenciaBloqueada = false;
                }
                boolean permitirCorrecao = segurandoBack
                        && sinalConfirmado
                        && imuDisponivel
                        && leituraValida
                        && !assistenciaBloqueada
                        && Math.abs(xBruto) <= DEAD_ZONE
                        && Math.abs(yBruto) > 0.12;

                double erroRumo = 0.0;
                if (permitirCorrecao) {
                    if (!assistenciaAtiva) {
                        rumoAlvo = yaw;
                        assistenciaAtiva = true;
                    }
                    erroRumo = normalizarAngulo(rumoAlvo - yaw);
                    if (Math.abs(erroRumo) > LIMITE_ERRO_RUMO) {
                        assistenciaAtiva = false;
                        assistenciaBloqueada = true;
                    } else {
                        x = Range.clip(sinalYawPorX * KP_RUMO * erroRumo,
                                -MAX_CORRECAO_X, MAX_CORRECAO_X);
                    }
                } else {
                    assistenciaAtiva = false;
                }
                if (!leituraValida) {
                    assistenciaAtiva = false;
                    assistenciaBloqueada = segurandoBack;
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
                double alvoMD = Range.clip(potenciaMD * velocidade, -1, 1);
                double alvoME = Range.clip(potenciaME * velocidade, -1, 1);
                potenciaAnteriorMD = aplicarRampa(potenciaAnteriorMD, alvoMD, dt);
                potenciaAnteriorME = aplicarRampa(potenciaAnteriorME, alvoME, dt);
                MD.setPower(potenciaAnteriorMD);
                ME.setPower(potenciaAnteriorME);

                telemetry.addData("Velocidade", "%.0f%%", velocidade * 100);
                telemetry.addData("Curva quadratica", curvaQuadratica);
                telemetry.addData("Yaw", "%.2f", yaw);
                telemetry.addData("Sinal IMU", sinalConfirmado ? sinalYawPorX : 0);
                telemetry.addData("BACK assistencia", assistenciaAtiva);
                telemetry.addData("Bloqueada ate soltar BACK", assistenciaBloqueada);
                telemetry.addData("Erro rumo", "%.2f", erroRumo);
                telemetry.addData("MD / ME", "%.2f / %.2f",
                        potenciaAnteriorMD, potenciaAnteriorME);
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
        return Range.clip(valor, -1, 1);
    }

    private static double quadradoComSinal(double valor) {
        return Math.copySign(valor * valor, valor);
    }

    private static double normalizarAngulo(double valor) {
        if (!Double.isFinite(valor)) {
            return Double.NaN;
        }
        valor %= 360.0;
        if (valor > 180.0) valor -= 360.0;
        if (valor < -180.0) valor += 360.0;
        return valor;
    }

    private static double aplicarRampa(double atual, double alvo, double dt) {
        boolean invertendo = atual != 0.0 && alvo != 0.0
                && Math.signum(atual) != Math.signum(alvo);
        double destino = invertendo ? 0.0 : alvo;
        boolean acelerando = (atual == 0.0 && destino != 0.0)
                || (Math.signum(atual) == Math.signum(destino)
                    && Math.abs(destino) > Math.abs(atual));
        double taxa = acelerando ? RAMPA_ACELERACAO : RAMPA_FRENAGEM;
        double passo = taxa * Range.clip(dt, 0.0, 0.10);
        return Range.clip(destino, atual - passo, atual + passo);
    }
}
