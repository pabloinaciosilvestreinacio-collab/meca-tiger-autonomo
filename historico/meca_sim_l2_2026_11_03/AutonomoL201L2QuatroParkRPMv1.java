package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * MECA TIGER FTC #32578 - L2-01 V1 - BASE DE TESTE MECA SIM.
 * Largada na zona L2, ja apontado para o HIVE -> 4 POLLEN -> PARK.
 * IMPORTANTISSIMO: configurar no Meca Sim X=1.574, Y=3.420, yaw=180.
 * O Java NAO altera a pose inicial do simulador.
 * Verificacao visual de TIP oposto AINDA NAO IMPLEMENTADA.
 * O registro de 4 pulsos NAO confirma 4 elementos ejetados nem TIP.
 * NAO USAR NO ROBO REAL ANTES DE CALIBRAR RPM, POSE E ROTA.
 */
@Autonomous(name = "TESTE L2-01 V1 4 POLLEN PARK", group = "Meca Tiger")
public class AutonomoL201L2QuatroParkRPMv1 extends LinearOpMode {

    private static final double LIMITE_TOTAL = 29.30;
    private static final double RESERVA_PARK = 5.00;
    private static final double ARENA = 3.660;
    private static final double CHASSI = 0.450;
    private static final double X_INICIAL = 1.574;
    private static final double Y_INICIAL = 3.420;
    private static final double YAW_INICIAL = 180.0;
    private static final double X_PARK = 0.255;
    private static final double Y_PARK = 2.380;
    private static final double YAW_PARK = 90.0;

    private static final double TICKS_M = 560.0 / (Math.PI * 0.090);
    private static final double TICKS_VOLTA_MS_SIM = 560.0;
    private static final double RPM_ALVO_SIM = 127.0;
    private static final double RPM_MIN = RPM_ALVO_SIM * 0.88;
    private static final double RPM_MAX = RPM_ALVO_SIM * 1.10;
    private static final double INTERVALO_TIROS = 0.84;
    private static final double JANELA_RPM = 0.080;
    private static final double POTENCIA_MS_BASE = 0.60;
    private static final double SA_FECHADO = 0.20;
    private static final double SA_ABERTO = 0.70;

    private DcMotor ME, MD, MI, MS;
    private Servo SA;
    private IMU imu;
    private ElapsedTime global;
    private double yawOffset;
    private double x = X_INICIAL;
    private double y = Y_INICIAL;
    private double odomYaw = YAW_INICIAL;
    private int encoderE, encoderD;
    private int encoderMS;
    private double instanteRPM;
    private double instanteMudancaMS;
    private double rpmMS;
    private int rpmEstavel;
    private boolean shooterAtivo;
    private boolean alimentando;
    private String etapa = "INIT";
    private String falha = "NENHUMA";
    private String tipOposto = "NAO VERIFICADO (SEM VISAO)";
    private int pulsosL2 = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        try {
            ME = hardwareMap.get(DcMotor.class, "ME");
            MD = hardwareMap.get(DcMotor.class, "MD");
            MI = hardwareMap.get(DcMotor.class, "MI");
            MS = hardwareMap.get(DcMotor.class, "MS");
            SA = hardwareMap.get(Servo.class, "SA");
            imu = hardwareMap.get(IMU.class, "imu");

            ME.setDirection(DcMotor.Direction.FORWARD);
            MD.setDirection(DcMotor.Direction.REVERSE);
            MI.setDirection(DcMotor.Direction.FORWARD);
            MS.setDirection(DcMotor.Direction.FORWARD);
            ME.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            MD.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            ME.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            MD.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            MS.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            MI.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            pararTudo();

            imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.UP
            )));
            telemetry.addLine("L2-01 V1: inicio no L2, 4 POLLEN, PARK");
            telemetry.addData("Configurar pose Meca Sim", "X=1.574 Y=3.420 YAW=180");
            telemetry.addData("TIP oposto", tipOposto);
            telemetry.update();

            waitForStart();
            if (isStopRequested()) return;
            global = new ElapsedTime();
            // IMU pode iniciar em zero MESMO com robo voltado para -Y.
            // Offset mantem o mapa de L2 com yaw=180 sem girar o robo.
            yawOffset = norm(YAW_INICIAL - yawBruto());
            odomYaw = yaw();
            encoderE = ME.getCurrentPosition();
            encoderD = MD.getCurrentPosition();

            etapa = "Conferir posicao inicial L2";
            atualizarOdom();
            if (!poseL2Valida()) {
                falhar("Pose/rumo inicial L2 fora do limite");
                return;
            }

            // Nesta V1 nao ha sensor/camera capaz de verificar TIP.
            // Nao fabricar um resultado: prosseguir com quatro lancamentos.
            etapa = "4 POLLEN em L2";
            boolean disparos = dispararQuatro(LIMITE_TOTAL - RESERVA_PARK);
            desligarShooter();
            if (!opModeIsActive()) return;

            etapa = "Saida L2 -> PARK";
            // Em caso de disparo parcial, tenta estacionar mesmo assim.
            boolean park = seguirPark();
            etapa = park ? "PARK concluido (pose estimada)"
                         : "PARK nao confirmado";
            telemetriaFinal(disparos, park);
        } finally {
            pararTudo();
            if (global != null) {
                telemetry.addData("FIM", etapa);
                telemetry.addData("Tempo (s)", global.seconds());
                telemetry.addData("Pulsos L2", pulsosL2);
                telemetry.addData("Motivo", falha);
                telemetry.update();
            }
        }
    }

    private boolean dispararQuatro(double prazo) {
        if (!poseL2Valida()) return falhar("Fora da pose L2");
        pararTracao();
        MI.setPower(0);
        SA.setPosition(SA_FECHADO);
        ligarShooter();
        double ultimoPulso = -10.0;

        for (int tiro = 1; tiro <= 4; tiro++) {
            double instantePermitido = tiro == 1 ? global.seconds()
                : ultimoPulso + INTERVALO_TIROS;
            while (opModeIsActive() && global.seconds() < prazo) {
                atualizarOdom();
                atualizarRPM();
                if (!poseL2Valida()) return falhar("Saiu da pose L2 durante disparo");
                if (global.seconds() >= instantePermitido && rpmEstavel >= 3)
                    break;
                idle();
            }
            if (!opModeIsActive() || global.seconds() >= prazo)
                return falhar("Prazo/RPM bloqueou tiro " + tiro);
            if (rpmEstavel < 3) return falhar("RPM insuficiente tiro " + tiro);
            if (Math.abs(ME.getPower()) > 0.06 || Math.abs(MD.getPower()) > 0.06)
                return falhar("Tracao ligada durante disparo");

            double duracao = tiro == 4 ? 0.32 : 0.14;
            // Nao iniciar a alimentacao se o pulso inteiro nao couber.
            if (global.seconds() + 0.09 + duracao + (tiro == 4 ? 0.16 : 0) >= prazo)
                return falhar("Janela insuficiente para tiro " + tiro);

            alimentando = true;
            MI.setPower(1.00);
            if (!esperar(0.09, prazo)) return falhar("Pre-alimentacao expirada");
            SA.setPosition(SA_ABERTO);
            ultimoPulso = global.seconds();
            if (!esperar(duracao, prazo)) return falhar("Pulso cortado " + tiro);
            fecharAlimentador();
            pulsosL2++;
            rpmEstavel = 0;
            if (tiro == 4 && !esperar(0.16, prazo))
                return falhar("Finalizacao do quarto pulso expirada");
            telemetry.addData("L2 tiro", tiro + "/4");
            telemetry.addData("RPM", rpmMS);
            telemetry.addData("Tempo", global.seconds());
            telemetry.update();
        }
        return true;
    }

    private boolean seguirPark() {
        // Curva de referencia da familia L1, agora usada APOS partir de L2.
        // Testar no Meca Sim: a folga do HIVE ainda nao foi validada no L2-01.
        final double iniX = x, iniY = y;
        final double p1x = 1.574, p1y = 2.650;
        final double p2x = 0.620, p2y = 2.700;
        final double fimX = X_PARK, fimY = Y_PARK;
        ElapsedTime trecho = new ElapsedTime();
        int indice = 0;
        double ultimoX = x, ultimoY = y, tempoMovimento = 0;
        while (opModeIsActive() && !prazoEsgotado()
               && trecho.seconds() < 9.0) {
            atualizarOdom();
            double falta = Math.hypot(fimX - x, fimY - y);
            double erroFinal = Math.abs(norm(YAW_PARK - yaw()));
            if (falta <= 0.035 && erroFinal <= 2.5) {
                pararTracao();
                return true;
            }
            double a = Math.toRadians(yaw());
            double meia = CHASSI / 2.0 *
                (Math.abs(Math.cos(a)) + Math.abs(Math.sin(a)));
            if (x - meia < 0.008 || x + meia > ARENA - 0.008 ||
                y - meia < 0.008 || y + meia > ARENA - 0.008)
                return falhar("Risco de parede na curva PARK");

            int melhor = indice;
            double melhorD2 = Double.POSITIVE_INFINITY;
            for (int i = Math.max(0, indice - 3); i <= 120; i++) {
                double t = i / 120.0;
                double bx = bezier(iniX, p1x, p2x, fimX, t);
                double by = bezier(iniY, p1y, p2y, fimY, t);
                double d2 = (bx - x) * (bx - x) + (by - y) * (by - y);
                if (d2 < melhorD2) { melhorD2 = d2; melhor = i; }
            }
            indice = Math.max(indice, melhor);
            double vista = Math.min(1.0, (indice + 6) / 120.0);
            double alvoX = bezier(iniX, p1x, p2x, fimX, vista);
            double alvoY = bezier(iniY, p1y, p2y, fimY, vista);
            double dx = alvoX - x, dy = alvoY - y;
            double rumo = falta < 0.15 ? YAW_PARK
                : norm(Math.toDegrees(Math.atan2(-dx, dy)));
            double erro = norm(rumo - yaw());
            double correcao = Range.clip(0.020 * Range.clip(erro, -24, 24), -0.32, 0.32);
            double rampa = Range.clip(0.29 + trecho.seconds() * 1.8, 0.29, 0.94);
            double freio = Math.sqrt(2.0 * 1.05 * Math.max(0, falta));
            double potencia = Math.min(0.94, Math.min(rampa, Math.max(0.13, freio)));
            if (x < 0.53) potencia = Math.min(potencia, 0.34);
            if (x < 0.36) potencia = Math.min(potencia, 0.20);
            if (Math.abs(erro) > 16) potencia = Math.min(potencia, 0.32);
            ME.setPower(Range.clip(potencia - correcao, -1, 1));
            MD.setPower(Range.clip(potencia + correcao, -1, 1));

            if (Math.hypot(x - ultimoX, y - ultimoY) >= 0.012) {
                ultimoX = x; ultimoY = y; tempoMovimento = trecho.seconds();
            }
            if (trecho.seconds() > 0.85 && trecho.seconds() - tempoMovimento > 0.75)
                return falhar("PARK sem progresso nos encoders");
            idle();
        }
        return falhar("PARK timeout/limite global");
    }

    private void ligarShooter() {
        shooterAtivo = true;
        alimentando = false;
        encoderMS = MS.getCurrentPosition();
        instanteRPM = global.seconds();
        instanteMudancaMS = instanteRPM;
        rpmEstavel = 0;
        MS.setPower(POTENCIA_MS_BASE);
    }

    private void atualizarRPM() {
        if (!shooterAtivo) return;
        double agora = global.seconds();
        int pos = MS.getCurrentPosition();
        if (agora - instanteRPM >= JANELA_RPM) {
            double dt = agora - instanteRPM;
            int delta = Math.abs(pos - encoderMS);
            rpmMS = delta * 60.0 / (TICKS_VOLTA_MS_SIM * dt);
            if (delta >= 2) instanteMudancaMS = agora;
            encoderMS = pos;
            instanteRPM = agora;
            rpmEstavel = rpmMS >= RPM_MIN && rpmMS <= RPM_MAX
                ? Math.min(3, rpmEstavel + 1) : 0;
            double potencia = Range.clip(
                POTENCIA_MS_BASE + 0.0015 * (RPM_ALVO_SIM - rpmMS), 0.48, 0.72);
            MS.setPower(potencia);
        }
        // Intertravamento de coleta / alimentacao: MI+MS so em tiro.
        if (!alimentando && MI.getPower() > 0.50 && MS.getPower() > 0.50) {
            fecharAlimentador();
            desligarShooter();
            falha = "Intertravamento MI/MS";
        }
        if (agora - instanteMudancaMS > 0.60) {
            desligarShooter();
            falha = "Encoder shooter sem progresso";
        }
    }

    private void atualizarOdom() {
        int e = ME.getCurrentPosition(), d = MD.getCurrentPosition();
        double atual = yaw();
        double ds = ((e - encoderE) + (d - encoderD)) / (2.0 * TICKS_M);
        double meio = odomYaw + norm(atual - odomYaw) / 2.0;
        x += -Math.sin(Math.toRadians(meio)) * ds;
        y += Math.cos(Math.toRadians(meio)) * ds;
        encoderE = e; encoderD = d; odomYaw = atual;
    }

    private boolean poseL2Valida() {
        atualizarOdom();
        double a = Math.toRadians(yaw());
        double meia = CHASSI / 2.0 *
            (Math.abs(Math.cos(a)) + Math.abs(Math.sin(a)));
        double folgaSup = ARENA - (y + meia);
        return Math.abs(x - X_INICIAL) <= 0.055
            && Math.abs(y - Y_INICIAL) <= 0.045
            && Math.abs(norm(YAW_INICIAL - yaw())) <= 2.5
            && folgaSup >= 0.008 && folgaSup <= 0.046;
    }

    private boolean esperar(double duracao, double prazo) {
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && !prazoEsgotado()
               && global.seconds() < prazo && timer.seconds() < duracao) {
            atualizarRPM();
            idle();
        }
        return opModeIsActive() && !prazoEsgotado()
            && global.seconds() < prazo && timer.seconds() >= duracao;
    }

    private void desligarShooter() {
        shooterAtivo = false;
        fecharAlimentador();
        if (MS != null) MS.setPower(0);
    }

    private void fecharAlimentador() {
        alimentando = false;
        if (MI != null) MI.setPower(0);
        if (SA != null) SA.setPosition(SA_FECHADO);
    }

    private void pararTracao() {
        if (ME != null) ME.setPower(0);
        if (MD != null) MD.setPower(0);
    }

    private void pararTudo() {
        pararTracao();
        desligarShooter();
    }

    private boolean falhar(String motivo) {
        falha = motivo;
        pararTracao();
        telemetry.addData("FALHA", motivo);
        telemetry.addData("Etapa", etapa);
        telemetry.addData("Tempo", global == null ? 0 : global.seconds());
        telemetry.update();
        return false;
    }

    private boolean prazoEsgotado() {
        return global != null && global.seconds() >= LIMITE_TOTAL;
    }

    private double yawBruto() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private double yaw() { return norm(yawBruto() + yawOffset); }

    private static double norm(double ang) {
        while (ang > 180) ang -= 360;
        while (ang < -180) ang += 360;
        return ang;
    }

    private static double bezier(double a, double b, double c, double d, double t) {
        double u = 1.0 - t;
        return u * u * u * a + 3 * u * u * t * b
            + 3 * u * t * t * c + t * t * t * d;
    }

    private void telemetriaFinal(boolean quatro, boolean park) {
        telemetry.addData("Versao", "L2-01 V1");
        telemetry.addData("L2 disparos (pulsos)", pulsosL2);
        telemetry.addData("Tiros completados", quatro);
        telemetry.addData("PARK estimado", park);
        telemetry.addData("Pose final", String.format("X=%.3f Y=%.3f yaw=%.1f", x, y, yaw()));
        telemetry.addData("TIP oposto", tipOposto);
        telemetry.addData("IMPORTANTE", "Pulsos nao comprovam POLLEN/TIP");
        telemetry.addData("Falha", falha);
        telemetry.update();
    }
}
