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
 * MECA TIGER FTC #32578 - L2-03 V1 - GARDEN + 8 POLLEN, SEM CAMERA.
 * L2: 4 POLLEN -> Garden -> 4 POLLEN L2 -> PARK somente se houver tempo.
 * IMPORTANTISSIMO: configurar no Meca Sim X=1.574, Y=3.420, yaw=180.
 * O Java NAO altera a pose inicial do simulador.
 * Esta versao NAO usa camera nem bibliotecas de visao.
 * O TIP permanece NAO VERIFICADO e exige teste separado.
 * Nao existe espera por visao, nem dependencia de hardware de camera.
 * O registro de 4 pulsos NAO confirma 4 elementos ejetados nem TIP.
 * NAO USAR NO ROBO REAL ANTES DE CALIBRAR RPM, POSE E ROTA.
 */
@Autonomous(name = "TESTE L2-03 V1 GARDEN SEM CAMERA", group = "Meca Tiger")
public class AutonomoL203Garden8PollenSemCameraRPMv1 extends LinearOpMode {

    private static final double LIMITE_TOTAL = 29.30;
    // PARK condicional: so iniciar com margem real suficiente.
    private static final double RESERVA_PARK = 4.00;
    private static final double ARENA = 3.660;
    private static final double CHASSI = 0.450;
    private static final double X_INICIAL = 1.574;
    private static final double Y_INICIAL = 3.420;
    private static final double YAW_INICIAL = 180.0;
    private static final double X_PARK = 0.255;
    private static final double Y_PARK = 2.380;
    private static final double YAW_PARK = 90.0;
    // Geometria Garden importada da familia L1-05, espelhando somente
    // o trajeto de acesso: L2 -> Garden em frente, Garden -> L2 em re.
    private static final double X_GARDEN_INICIO = 1.530;
    private static final double X_GARDEN_RETORNO = 1.420;
    private static final double X_GARDEN_FIM = 0.285;
    private static final double Y_PREPARACAO_GARDEN = 0.450;
    private static final double Y_GARDEN_COLETA = 0.262;
    private static final double COMPRIMENTO_RAMPA_GARDEN = 1.050;
    private static final double FOLGA_MIN_GARDEN = 0.012;
    private static final double MARGEM_PAREDE = 0.010;
    // Nao iniciar o Garden caso a primeira sequencia demore demais.
    // Este corte preserva a possibilidade de terminar a segunda sequencia.
    private static final double LIMITE_PARTIDA_GARDEN = 7.00;
    private static final double MARGEM_FIM = 0.15;

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
    private int pulsosL2 = 0;
    private int pulsosPrimeiroL2 = 0;
    private int pulsosSegundoL2 = 0;
    private boolean gardenPercorrido = false;
    private boolean parkOmitido = false;
    private double instantePrimeiroL2 = -1;
    private double instanteGarden = -1;
    private double instanteSegundoL2 = -1;
    private double instantePark = -1;

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
            telemetry.addLine("L2-03 V1: 4 L2 -> GARDEN -> 4 L2 -> PARK condicional");
            telemetry.addData("Spawn Meca Sim", "X=1.574 Y=3.420 yaw=180");
            telemetry.addData("Camera/TIP", "SEM CAMERA / NAO VERIFICADO");
            telemetry.update();

            waitForStart();
            if (isStopRequested()) return;
            global = new ElapsedTime();
            // Compensa o yaw que a IMU apresenta na pose do simulador;
            // NAO descobre a posicao real de largada automaticamente.
            yawOffset = norm(YAW_INICIAL - yawBruto());
            odomYaw = yaw();
            encoderE = ME.getCurrentPosition();
            encoderD = MD.getCurrentPosition();

            etapa = "L2 inicial: validar pose";
            atualizarOdom();
            if (!poseL2Valida()) {
                falhar("Largada nao confere com pose L2 configurada");
                return;
            }

            etapa = "L2: primeiros 4 POLLEN";
            boolean primeiros = dispararQuatro(LIMITE_PARTIDA_GARDEN);
            pulsosPrimeiroL2 = pulsosL2;
            instantePrimeiroL2 = global.seconds();
            desligarShooter();
            if (!opModeIsActive()) return;

            if (!primeiros || global.seconds() > LIMITE_PARTIDA_GARDEN) {
                // Nao partir em um ciclo longo a partir de uma primeira
                // etapa incompleta ou muito atrasada.
                etapa = "Garden cancelado: primeiros tiros/prazo";
                boolean park = parkSeHouverTempo();
                telemetriaFinal(false, park);
                return;
            }

            etapa = "L2 -> Garden: aproximacao longa";
            double yEntrada = linhaGardenY(X_GARDEN_RETORNO);
            if (!seguirBezier(
                    "L2 -> Garden", false,
                    X_INICIAL, 1.300, 1.830, Y_PREPARACAO_GARDEN,
                    X_GARDEN_RETORNO, yEntrada, 0.86, 90.0,
                    7.0, 10, 0.025)) {
                // Sem recuperacao cega caso o robot fique fora da rota.
                etapa = "Falha acesso Garden: parar seguro";
                pararTudo();
                telemetriaFinal(false, false);
                return;
            }

            etapa = "Garden: coletar em -X e retornar em re";
            if (!executarGarden()) {
                etapa = "Garden incompleto: parar seguro";
                pararTudo();
                telemetriaFinal(false, false);
                return;
            }
            gardenPercorrido = true;
            instanteGarden = global.seconds();

            // Garden -> L2: mesma curva geometrica da familia L1,
            // marcha em re; MI OFF durante aquecimento do shooter.
            etapa = "Garden -> L2 em curva de re";
            if (!seguirBezier(
                    "Garden -> L2", true,
                    x + 0.410, y, X_INICIAL, 1.300,
                    X_INICIAL, Y_INICIAL, 0.96, 180.0,
                    7.0, 10, 0.018)) {
                etapa = "Falha retorno L2: parar seguro";
                pararTudo();
                telemetriaFinal(false, false);
                return;
            }
            pararTracao();
            if (!poseL2Valida()) {
                falhar("Retorno Garden terminou fora da pose L2");
                telemetriaFinal(false, false);
                return;
            }

            pulsosL2 = 0;
            etapa = "L2: ultimos 4 POLLEN apos Garden";
            boolean segundos = dispararQuatro(LIMITE_TOTAL - MARGEM_FIM);
            pulsosSegundoL2 = pulsosL2;
            instanteSegundoL2 = global.seconds();
            desligarShooter();
            if (!opModeIsActive()) return;

            // PARK nao tem prioridade sobre os 8 disparos, nem pode
            // comecar quando ja nao ha margem de percurso.
            boolean park = parkSeHouverTempo();
            if (park) etapa = "PARK concluido (pose estimada)";
            else etapa = "Fim L2: parado / PARK omitido ou falhou";
            telemetriaFinal(segundos, park);
        } finally {
            pararTudo();
            if (global != null) {
                telemetry.addData("VERSAO", "L2-03 V1 GARDEN SEM CAMERA");
                telemetry.addData("FIM", etapa);
                telemetry.addData("Tempo", global.seconds());
                telemetry.addData("Pulsos 1/2", pulsosPrimeiroL2 + "/" + pulsosSegundoL2);
                telemetry.addData("Garden concluido", gardenPercorrido);
                telemetry.addData("PARK omitido", parkOmitido);
                telemetry.addData("Falha", falha);
                telemetry.update();
            }
        }
    }

    // Retorna true apenas se o seguidor de estacionamento concluiu;
    // nenhum deslocamento eh iniciado se a margem for pequena.
    private boolean parkSeHouverTempo() {
        desligarShooter();
        MI.setPower(0);
        SA.setPosition(SA_FECHADO);
        pararTracao();
        atualizarOdom();
        if (!opModeIsActive() || !poseL2Valida()
                || global.seconds() > LIMITE_TOTAL - RESERVA_PARK) {
            parkOmitido = true;
            etapa = "PARK omitido (sem tempo/pose L2)";
            return false;
        }
        etapa = "PARK condicional L2 -> esquerda";
        instantePark = global.seconds();
        boolean ok = seguirPark();
        if (!ok) parkOmitido = true;
        return ok;
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
                if (!shooterAtivo)
                    return falhar("Shooter desligou durante espera RPM");
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
            if (!esperar(0.09, prazo)) {
                fecharAlimentador();
                return falhar("Pre-alimentacao expirada");
            }
            SA.setPosition(SA_ABERTO);
            ultimoPulso = global.seconds();
            if (!esperar(duracao, prazo)) {
                fecharAlimentador();
                return falhar("Pulso cortado " + tiro);
            }
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

    // =========================================================
    // L2-03: Garden, percurso original da familia L1 invertido.
    // Seguir a faixa ao lado da parede inferior do campo (Y baixo).
    // =========================================================
    private double linhaGardenY(double px) {
        double t = Range.clip((X_GARDEN_INICIO - px)
            / COMPRIMENTO_RAMPA_GARDEN, 0.0, 1.0);
        double suave = t*t*(3.0-2.0*t);
        return Y_PREPARACAO_GARDEN
            + (Y_GARDEN_COLETA - Y_PREPARACAO_GARDEN)*suave;
    }

    private boolean executarGarden() {
        // Nunca ligar MI e MS juntos durante a coleta.
        desligarShooter();
        MI.setPower(1.00);
        SA.setPosition(SA_FECHADO);
        try {
            if (!percorrerGardenGuiado(false)) return false;
            if (!percorrerGardenGuiado(true)) return false;
            return true;
        } finally {
            MI.setPower(0);
            SA.setPosition(SA_FECHADO);
        }
    }

    private boolean percorrerGardenGuiado(boolean re) {
        etapa = re ? "Garden: retorno em re" : "Garden: coleta em -X";
        ElapsedTime trecho = new ElapsedTime();
        double ultimoX = x, instanteAvanco = 0;
        double timeout = re ? 4.4 : 4.8;
        while (opModeIsActive() && !prazoEsgotado()
                && trecho.seconds() < timeout) {
            atualizarOdom();
            double resto = Math.abs((re ? X_GARDEN_RETORNO : X_GARDEN_FIM) - x);
            if (resto < 0.013) {
                pararTracao();
                telemetry.addData("Garden", re ? "RECUO OK" : "COLETA OK");
                telemetry.addData("t", global.seconds());
                telemetry.update();
                return true;
            }
            double a = Math.toRadians(yaw());
            double meia = CHASSI/2.0 *
                (Math.abs(Math.sin(a))+Math.abs(Math.cos(a)));
            double inf = y-meia;
            double esq = x-meia;
            double dir = ARENA-x-meia;
            if (inf < FOLGA_MIN_GARDEN || esq < FOLGA_MIN_GARDEN
                    || dir < FOLGA_MIN_GARDEN)
                return falhar("Garden: folga da parede insuficiente");

            double vistaX = Range.clip(x+(re ? 0.11 : -0.11),
                                      X_GARDEN_FIM, X_GARDEN_INICIO);
            double dx = vistaX-x;
            double dy = linhaGardenY(vistaX)-y;
            double rumo = norm(Math.toDegrees(Math.atan2(
                re ? dx : -dx, re ? -dy : dy)));
            // Ao sair do Garden em re, limitar o angulo da dianteira
            // perto da parede inferior: o chassi de 45 cm aumenta sua
            // projeção vertical ao sair de 90 graus.
            if (re && y < 0.36) {
                double c = Range.clip((y - 0.020)
                    / (0.225*Math.sqrt(2.0)),
                    Math.sqrt(0.5), 1.0);
                double desvioMax = Math.max(1.0,
                    Math.toDegrees(Math.asin(c)) - 45.0);
                rumo = Range.clip(rumo,
                    90.0-desvioMax, 90.0+desvioMax);
            }
            double erro = Range.clip(norm(rumo-yaw()),-14,14);
            double correcao = Range.clip(0.019*erro,-0.19,0.19);
            double rampa = Range.clip(0.22+trecho.seconds()*1.6,0.22,1.0);
            double freio = Math.sqrt(2.0*Math.max(0,resto));
            double v = Math.min(re ? 0.75 : 0.67,
                Math.min(rampa,Math.max(0.14,freio)));
            if (inf < 0.040 || Math.abs(erro)>7.0)
                v = Math.min(v,0.25);
            double base = (re ? -1.0 : 1.0)*v;
            ME.setPower(Range.clip(base-correcao,-1,1));
            MD.setPower(Range.clip(base+correcao,-1,1));

            if (Math.abs(x-ultimoX)>=0.014) {
                ultimoX=x; instanteAvanco=trecho.seconds();
            }
            if (trecho.seconds()>0.8 && trecho.seconds()-instanteAvanco>0.70)
                return falhar("Garden: encoders sem progresso");
            idle();
        }
        return falhar("Garden: timeout/limite global");
    }

    private double folgaChassi() {
        double a = Math.toRadians(yaw());
        double meia = CHASSI/2.0 *
            (Math.abs(Math.cos(a)) + Math.abs(Math.sin(a)));
        return Math.min(
            Math.min(x-meia, ARENA-x-meia),
            Math.min(y-meia, ARENA-y-meia));
    }

    // Seguidor Bezier compartilhado, sem strafe; aceita marcha em re.
    private boolean seguirBezier(String nome, boolean re,
        double p1x,double p1y,double p2x,double p2y,
        double fimX,double fimY,double potenciaMax,
        double yawFinal,double timeout,int antecipacao,
        double toleranciaFinal) {
        atualizarOdom();
        final double iniX=x,iniY=y;
        ElapsedTime timer=new ElapsedTime();
        double ux=x,uy=y,progresso=0;
        int indice=0;
        while (opModeIsActive() && !prazoEsgotado()
                && timer.seconds() < timeout) {
            atualizarOdom();
            double falta=Math.hypot(fimX-x,fimY-y);
            if (falta<=toleranciaFinal
                    && Math.abs(norm(yawFinal-yaw()))<=2.5) {
                pararTracao(); return true;
            }
            if (folgaChassi()<0.008)
                return falhar(nome+": parede proxima");
            // A entrada do Garden termina perto da parede inferior;
            // so nela alinhar o yaw ANTES de deslocar o ultimo trecho.
            // Nao girar em L2 encostado na parede superior.
            if (!re && fimY < 0.80 && falta < 0.22
                    && Math.abs(norm(yawFinal-yaw())) > 4.0) {
                double e = norm(yawFinal-yaw());
                double giro = Range.clip(0.023*e,-0.40,0.40);
                if (Math.abs(giro) < 0.095) giro = Math.copySign(0.095,e);
                ME.setPower(-giro);
                MD.setPower(giro);
                idle();
                continue;
            }
            int melhor=indice;double melhorD2=Double.POSITIVE_INFINITY;
            for(int i=Math.max(0,indice-3);i<=120;i++) {
                double t=i/120.0;
                double bx=bezier(iniX,p1x,p2x,fimX,t);
                double by=bezier(iniY,p1y,p2y,fimY,t);
                double d2=(bx-x)*(bx-x)+(by-y)*(by-y);
                if (d2<melhorD2) {melhorD2=d2;melhor=i;}
            }
            indice=Math.max(indice,melhor);
            double vista=Math.min(1.0,(indice+antecipacao)/120.0);
            double bx=bezier(iniX,p1x,p2x,fimX,vista);
            double by=bezier(iniY,p1y,p2y,fimY,vista);
            double dx=bx-x,dy=by-y;
            double rumo=falta<0.15?yawFinal
                :norm(Math.toDegrees(Math.atan2(
                    re?dx:-dx,re?-dy:dy)));
            double erro=norm(rumo-yaw());
            double corr=Range.clip(0.020*Range.clip(erro,-24,24),-0.32,0.32);
            double rampa=Range.clip(0.29+timer.seconds()*1.8,0.29,potenciaMax);
            double freio=Math.sqrt(2.0*1.05*Math.max(0,falta));
            double v=Math.min(potenciaMax,Math.min(rampa,Math.max(0.13,freio)));
            if(fimY>3.35&&y>3.00) v=Math.min(v,0.60);
            if(fimY>3.35&&y>3.30) v=Math.min(v,0.35);
            if(fimY>3.35&&y>3.385) v=Math.min(v,0.20);
            if(Math.abs(erro)>16) v=Math.min(v,0.32);
            if (!re && fimY < 0.80 && falta < 0.24)
                v = Math.min(v,0.22);
            if (!re && fimY < 0.80 && falta < 0.10)
                v = Math.min(v,0.13);
            double base=(re?-1.0:1.0)*v;
            ME.setPower(Range.clip(base-corr,-1,1));
            MD.setPower(Range.clip(base+corr,-1,1));
            if(Math.hypot(x-ux,y-uy)>=0.012) {
                ux=x;uy=y;progresso=timer.seconds();
            }
            if(timer.seconds()>0.85&&timer.seconds()-progresso>0.75)
                return falhar(nome+": sem progresso");
            idle();
        }
        return falhar(nome+": timeout ou alvo nao atingido");
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


    private void telemetriaFinal(boolean segundoCompleto, boolean park) {
        telemetry.addData("Versao", "L2-03 V1 GARDEN SEM CAMERA");
        telemetry.addData("Primeiros 4 pulsos", pulsosPrimeiroL2);
        telemetry.addData("Ultimos 4 pulsos", pulsosSegundoL2);
        telemetry.addData("Garden percorrido", gardenPercorrido);
        telemetry.addData("L2 segundo ciclo completo", segundoCompleto);
        telemetry.addData("PARK estimado", park);
        telemetry.addData("PARK omitido", parkOmitido);
        telemetry.addData("Tempo 1/Garden/2/PARK",
            instantePrimeiroL2+" / "+instanteGarden+" / "
            +instanteSegundoL2+" / "+instantePark);
        telemetry.addData("Tempo global", global.seconds());
        telemetry.addData("Pose final", String.format(
            "X=%.3f Y=%.3f yaw=%.1f",x,y,yaw()));
        telemetry.addData("TIP", "NAO VERIFICADO - SEM CAMERA");
        telemetry.addData("ALERTA", "Pulsos nao comprovam 8 POLLEN ou TIP");
        telemetry.addData("Falha", falha);
        telemetry.update();
    }
}
