
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
 * MECA TIGER FTC #32578 — L1-02 (Meca Sim)
 * QUATRO DISPAROS L1 -> GARDEN GUIADO -> PARK VERMELHO ESQUERDO.
 * DERIVADO DA VERSAO APROVADA L1-03, SEM ALTERA-LA.
 * Somente este novo OpMode foi criado.
 * Pose inicial no simulador: X=1.530, Y=0.255, yaw=0.
 * Chassi 0.45x0.45m; bitola 0.45m; 1m/s; 1.8m/s2.
 * IMPORTANTE: RPM 127 e calibracao do SIMULADOR, nao do robo fisico.
 */

@Autonomous(
    name = "TESTE L1-02 4 L1 GARDEN PARK RPM",
    group = "Meca Tiger"
)
public class AutonomoL102GardenParkRPM
        extends LinearOpMode {

    // ==========================================
    // CONSTANTES DE LOCOMOCAO
    // ==========================================

    private static final double TICKS_M =
            560.0 / (Math.PI * 0.090);

    private static final double X_START = 1.530;
    private static final double Y_START = 0.255;

    // PARK vermelho esquerdo marcado no L1-03.
    // Chegar pelo corredor esquerdo e fazer o ultimo trecho horizontal.
    private static final double X_CORREDOR_PARK = 0.550;
    private static final double Y_ENTRADA_CORREDOR = 0.550;
    private static final double X_PARK = 0.255;
    private static final double Y_PARK = 2.380;

    // Altura de preparacao da curva em S. A varredura acontece mais abaixo;
    // este ponto fornece espaco para aproximar o chassi sem tocar a parede.
    private static final double Y_PREPARACAO_GARDEN = 0.450;

    // A linha do Garden e paralela a parede Y=0.
    // O centro deve permanecer fora da parede (meio chassi = 0.225).
    // Ajuste este valor APOS medir a faixa do intake no robo real.
    private static final double Y_GARDEN_COLETA = 0.262;
    private static final double X_GARDEN_FIM = 0.285;
    private static final double X_GARDEN_RETORNO = 1.420;
    private static final double COMPRIMENTO_RAMPA_GARDEN = 1.050;
    private static final double FOLGA_MIN_GARDEN = 0.012;


    private static final double LIM_GLOBAL = 29.3;
    private static final double TOL_GRAUS = 1.5;

    // ==========================================
    // SHOOTER
    // ==========================================

    private static final double MS_SHOT = 0.60;
    private static final double MS_POWER_MAX = 0.72;

    private static final double MI_INTAKE = 1.00;
    private static final double MI_FEED = 1.00;

    private static final double SA_FECHADO = 0.20;
    private static final double SA_ABERTO = 0.70;

    // Valor exclusivo do perfil atual do Meca Sim. No robo fisico, medir e
    // confirmar o encoder/reducao do MS antes de reutilizar este numero.
    private static final double TICKS_POR_VOLTA_MS = 560.0;

    private static final double RPM_ALVO_MS_SIM = 127.0;

    private static final double RPM_MIN_MS =
            RPM_ALVO_MS_SIM * 0.88;

    private static final double RPM_MAX_MS =
            RPM_ALVO_MS_SIM * 1.10;

    private static final double KP_RPM_MS = 0.0015;
    private static final double JANELA_RPM = 0.080;
    private static final int AMOSTRAS_RPM_ESTAVEL = 3;
    private static final double TIMEOUT_ENCODER_MS = 0.60;

    private static final double INTERVALO_TIROS = 0.82;
    private static final double PULSO_ALIMENTADOR = 0.14;

    private static final int POLLENS_POR_POSTO = 4;

    // ==========================================
    // CHASSI E PAREDE L2
    // ==========================================

    private static final double ARENA = 3.660;
    private static final double CHASSI = 0.450;

    // Folga segura para impedir contato transitorio na parede de L2.

    // ==========================================
    // HARDWARE
    // ==========================================

    private DcMotor ME;
    private DcMotor MD;
    private DcMotor MI;
    private DcMotor MS;

    private Servo SA;
    private IMU imu;

    private ElapsedTime global;

    // ==========================================
    // ODOMETRIA
    // ==========================================

    private double estimX = X_START;
    private double estimY = Y_START;

    private int odomEncoderE;
    private int odomEncoderD;

    private double odomYaw;
    private double yawDosEncoders;

    // ==========================================
    // TELEMETRIA
    // ==========================================

    private String etapaAnterior = "Nenhuma";
    private String etapaAtual = "INIT";
    private String proximaEtapa = "Disparo L1";

    private String motivoFalha = "";

    private double rpmMS = 0;
    private double potenciaMSComandada = 0;
    private double rpmAntesDisparo = 0;
    private double rpmMinRecuperacao = Double.NaN;
    private int amostrasRpmEstaveis = 0;
    private boolean aguardandoRecuperacao = false;
    private String estadoShooter = "PARADO";

    private int pulsosL1 = 0;

    // ==========================================
    // PROGRAMA PRINCIPAL
    // ==========================================

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

            ME.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
            );

            MD.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
            );

            ME.setMode(
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            );

            MD.setMode(
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            );

            MI.setMode(
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            );

            MS.setMode(
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            );

            ME.setPower(0);
            MD.setPower(0);
            MI.setPower(0);
            MS.setPower(0);

            SA.setPosition(SA_FECHADO);

            imu.initialize(
                new IMU.Parameters(
                    new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot
                            .LogoFacingDirection.RIGHT,
                        RevHubOrientationOnRobot
                            .UsbFacingDirection.UP
                    )
                )
            );

            telemetry.addLine("MECA TIGER - L1-02 4 L1 RPM");
            telemetry.addLine("PARK VERMELHO");
            telemetry.addLine("GARDEN -> CORREDOR ESQUERDO -> PARK");
            telemetry.update();

            waitForStart();

            if (isStopRequested()) return;

            imu.resetYaw();
            global = new ElapsedTime();

            odomEncoderE = ME.getCurrentPosition();
            odomEncoderD = MD.getCurrentPosition();
            odomYaw = yaw();

            // ======================================
            // 1 - QUATRO DISPAROS EM L1
            // ======================================

            if (!dispararQuatro("L1", 4.20))
                return;

            MS.setPower(0);
            potenciaMSComandada = 0;
            estadoShooter = "DESLIGADO_APOS_L1";

            // ======================================
            // 2 - AVANCO CURTO: SOMENTE PARA LIBERAR O GIRO
            // ======================================

            etapaAnterior = etapaAtual;
            etapaAtual = "Saida curta de L1";
            proximaEtapa = "Giro para varredura Garden";

            if (!andar(
                X_START,
                Y_PREPARACAO_GARDEN,
                Y_PREPARACAO_GARDEN - Y_START,
                false,
                0.30,
                0.005
            )) return;

            // Nao inicia o giro enquanto ainda existe velocidade para frente.
            // Sem esta pausa o robo sobe cerca de 6 cm durante a virada.
            parar();

            for (int i = 0; i < 35 && opModeIsActive(); i++) {
                idle();
                atualizarOdom();
            }

            // ======================================
            // 3 - GIRO UNICO E VARREDURA RETA
            // ======================================

            if (!girar(90.0)) return;

            // ======================================
            // 4 - GARDEN: CURVA CONTINUA + VARREDURA
            // ======================================

            MI.setPower(MI_INTAKE);
            try {
                if (!percorrerGardenGuiado(false)) return;
            } finally {
                MI.setPower(0);
            }

            // ======================================
            // 5 - RETORNO PELO GARDEN, EXATAMENTE COMO L1-03
            // ======================================

            if (!percorrerGardenGuiado(true)) return;

            // ======================================
            // 6 - SAIR DO GARDEN PELO LADO ESQUERDO
            // Um unico segmento diagonal suave ate o corredor livre.
            // Nao atravessa o HIVE nem realiza giro perto da parede.
            // ======================================

            if (!segmento(
                "Garden -> acesso PARK",
                "Subida corredor esquerdo",
                X_CORREDOR_PARK,
                Y_ENTRADA_CORREDOR,
                false,
                0.82
            )) return;

            // ======================================
            // 7 - SUBIDA VERTICAL NO CORREDOR ESQUERDO
            // ======================================

            if (!segmento(
                "Corredor esquerdo -> pre-PARK",
                "Entrada horizontal no PARK",
                X_CORREDOR_PARK,
                Y_PARK,
                false,
                0.85
            )) return;

            // ======================================
            // 8 - ULTIMO TRECHO HORIZONTAL: PARK MARCADO
            // Giro acontece em X=0.55, NAO ao lado da parede.
            // ======================================

            if (!segmento(
                "Pre-PARK -> PARK vermelho",
                "Finalizar",
                X_PARK,
                Y_PARK,
                false,
                0.55
            )) return;

            parar();

            for (
                int i = 0;
                i < 4 && opModeIsActive();
                i++
            ) {

                idle();
                atualizarOdom();
            }

            double divergencia = norm(
                yaw() - yawDosEncoders
            );

            if (Math.abs(divergencia) > 2.5) {

                falha(
                    "SENSORES",
                    "IMU e encoders divergem: "
                        + divergencia
                );

                return;
            }

            double erroEstimado = Math.hypot(
                estimX - X_PARK,
                estimY - Y_PARK
            );

            if (erroEstimado > 0.05) {

                falha(
                    "ODOMETRIA",
                    "PARK fora da tolerancia de 5 cm"
                );

                return;
            }

            etapaAnterior = etapaAtual;
            etapaAtual = "TERMINOU NOMINALMENTE";
            proximaEtapa = "Conferir relatorio";

            telemetria(
                "L1-02: conferir 4 tiros L1, coleta e PARK"
            );

        } finally {

            parar();

            if (MI != null)
                MI.setPower(0);

            if (MS != null)
                MS.setPower(0);

            if (SA != null)
                SA.setPosition(SA_FECHADO);
        }
    }

    // ==========================================
    // DISPARO DOS QUATRO POLLEN
    // ==========================================

    private boolean dispararQuatro(
        String posto,
        double prazo
    ) {

        etapaAnterior = etapaAtual;
        etapaAtual = "Disparo 4 POLLEN " + posto;

        proximaEtapa = "Saida reta de L1";

        MI.setPower(0);
        SA.setPosition(SA_FECHADO);

        MS.setPower(MS_SHOT);
        potenciaMSComandada = MS_SHOT;

        double inicioAnterior = -10.0;
        int feitos = 0;

        for (
            int tiro = 1;
            tiro <= POLLENS_POR_POSTO;
            tiro++
        ) {

            double primeiroPermitido = tiro == 1
                ? 0.82
                : inicioAnterior + INTERVALO_TIROS;

            if (!aguardarRpmEIntervalo(
                primeiroPermitido,
                prazo
            )) {

                return falha(
                    "SHOOTER",
                    "RPM/tempo insuficiente: "
                        + feitos + "/4 em " + posto
                );
            }

            MI.setPower(MI_FEED);

            if (!esperarAte(
                0.025,
                prazo
            )) {
                MI.setPower(0);
                return falha("SHOOTER", "Pre-alimentacao expirada: " + posto + " tiro " + tiro);
            }

            SA.setPosition(SA_ABERTO);
            rpmAntesDisparo = rpmMS;
            inicioAnterior = global.seconds();

            if (!esperarAte(
                PULSO_ALIMENTADOR,
                prazo
            )) {

                MI.setPower(0);
                SA.setPosition(SA_FECHADO);
                return false;
            }

            MI.setPower(0);
            SA.setPosition(SA_FECHADO);
            aguardandoRecuperacao = true;
            rpmMinRecuperacao = Double.NaN;
            estadoShooter = "RECUPERANDO";

            feitos++;
            // Telemetria acompanha CADA pulso; nunca deixa 0/4 em falha tardia.
            pulsosL1 = feitos;

            telemetria(
                posto + ": " + feitos
                    + "/4; RPM=" + rpmMS
            );
        }

        pulsosL1 = feitos;

        if (feitos != 4) {

            return falha(
                "SHOOTER",
                posto + ": " + feitos + "/4 pulsos"
            );
        }

        return true;
    }

    // ==========================================
    // CONTROLE DE RPM
    // ==========================================

    private boolean aguardarRpmEIntervalo(
        double primeiroPermitido,
        double prazo
    ) {

        int encoderAnterior =
            MS.getCurrentPosition();

        int encoderInicial = encoderAnterior;
        double inicioMonitor = global.seconds();

        double tempoAnterior = global.seconds();

        amostrasRpmEstaveis = 0;
        estadoShooter = aguardandoRecuperacao
            ? "RECUPERANDO"
            : "SPINUP";

        while (
            opModeIsActive()
            && !prazoEsgotado()
            && global.seconds() < prazo
        ) {

            double agora = global.seconds();

            if (agora - tempoAnterior >= JANELA_RPM) {

                int encoderAtual =
                    MS.getCurrentPosition();

                double dt = agora - tempoAnterior;

                rpmMS =
                    Math.abs(
                        encoderAtual - encoderAnterior
                    ) * 60.0
                    / (TICKS_POR_VOLTA_MS * dt);

                encoderAnterior = encoderAtual;
                tempoAnterior = agora;

                double erroRpm =
                    RPM_ALVO_MS_SIM - rpmMS;

                potenciaMSComandada = Range.clip(
                    MS_SHOT + KP_RPM_MS * erroRpm,
                    0.48,
                    MS_POWER_MAX
                );

                MS.setPower(potenciaMSComandada);

                boolean rpmOk =
                    rpmMS >= RPM_MIN_MS
                    && rpmMS <= RPM_MAX_MS;

                amostrasRpmEstaveis = rpmOk
                    ? Math.min(
                        AMOSTRAS_RPM_ESTAVEL,
                        amostrasRpmEstaveis + 1
                    )
                    : 0;

                if (aguardandoRecuperacao) {
                    rpmMinRecuperacao = Double.isNaN(rpmMinRecuperacao)
                        ? rpmMS
                        : Math.min(rpmMinRecuperacao, rpmMS);
                }

                estadoShooter = rpmOk
                    ? "ESTABILIZANDO"
                    : (aguardandoRecuperacao
                        ? "RECUPERANDO"
                        : "SPINUP");
            }

            if (
                agora - inicioMonitor >= TIMEOUT_ENCODER_MS
                && Math.abs(
                    MS.getCurrentPosition() - encoderInicial
                ) < 2
            ) {
                estadoShooter = "ERRO_ENCODER";
                return false;
            }

            if (
                agora >= primeiroPermitido
                && amostrasRpmEstaveis >= AMOSTRAS_RPM_ESTAVEL
            ) {
                estadoShooter = "PRONTO";
                aguardandoRecuperacao = false;
                return true;
            }

            idle();
        }

        return false;
    }

    // ==========================================
    // ESPERA SEGURA
    // ==========================================

    private boolean esperarAte(
        double segundos,
        double prazo
    ) {

        ElapsedTime espera = new ElapsedTime();

        while (
            opModeIsActive()
            && !prazoEsgotado()
            && global.seconds() < prazo
            && espera.seconds() < segundos
        ) {
            idle();
        }

        return opModeIsActive()
            && !prazoEsgotado()
            && global.seconds() < prazo
            && espera.seconds() >= segundos;
    }

    // ==========================================
    // SEGMENTO COM GIRO
    // ==========================================

    private boolean segmento(
        String nome,
        String seguinte,
        double x,
        double y,
        boolean re,
        double maxPower
    ) {

        etapaAnterior = etapaAtual;
        etapaAtual = nome;
        proximaEtapa = seguinte;

        double dx = x - estimX;
        double dy = y - estimY;

        double distancia = Math.hypot(dx, dy);

        if (distancia < 0.01 || distancia > 4.0) {

            return falha(
                "GEOMETRIA",
                "Segmento invalido"
            );
        }

        double rumo = norm(
            Math.toDegrees(
                Math.atan2(
                    re ? dx : -dx,
                    re ? -dy : dy
                )
            )
        );

        if (!girar(rumo))
            return false;

        boolean feito = andar(
            x, y, distancia, re, maxPower
        );

        if (feito)
            telemetria("Segmento OK");

        return feito;
    }

    // ==========================================
    // DESLOCAMENTO SEM GIRO
    // ==========================================

    private boolean deslocarSemGirar(
        String nome,
        String seguinte,
        double metros,
        boolean re,
        double maxPower
    ) {

        etapaAnterior = etapaAtual;
        etapaAtual = nome;
        proximaEtapa = seguinte;

        atualizarOdom();

        double ang = Math.toRadians(yaw());
        double sentido = re ? -1.0 : 1.0;

        double alvoX =
            estimX - sentido * Math.sin(ang) * metros;

        double alvoY =
            estimY + sentido * Math.cos(ang) * metros;

        boolean feito = andar(
            alvoX, alvoY, metros, re, maxPower
        );

        if (feito)
            telemetria("Deslocamento reto OK");

        return feito;
    }

    // ==========================================
    // GARDEN - ENTRADA, COLETA E SAIDA GUIADAS
    // ==========================================

    // Coordenada Y de uma curva em S SUAVE, sem girar parado perto
    // da parede. A derivada vale zero no inicio e no fim da rampa.
    private double linhaGardenY(double px) {
        double s = Range.clip(
            (X_START - px) / COMPRIMENTO_RAMPA_GARDEN,
            0.0, 1.0
        );
        double suave = s * s * (3.0 - 2.0 * s);
        return Y_PREPARACAO_GARDEN
            + (Y_GARDEN_COLETA - Y_PREPARACAO_GARDEN) * suave;
    }

    // Margem entre a parede inferior e o retangulo completo do robo,
    // levando em conta o YAW. Nao confundir centro com borda frontal.
    private double folgaInferior() {
        double a = Math.toRadians(yaw());
        double metadeY = 0.5 * CHASSI * (
            Math.abs(Math.sin(a)) + Math.abs(Math.cos(a))
        );
        return estimY - metadeY;
    }

    private double folgaEsquerda() {
        double a = Math.toRadians(yaw());
        double metadeX = 0.5 * CHASSI * (
            Math.abs(Math.cos(a)) + Math.abs(Math.sin(a))
        );
        return estimX - metadeX;
    }

    private boolean percorrerGardenGuiado(boolean re) {
        etapaAnterior = etapaAtual;
        etapaAtual = re
            ? "Garden: retorno em re sem giro"
            : "Garden: coleta paralela a parede";
        proximaEtapa = re
            ? "Garden -> corredor HIVE"
            : "Retorno seguro do Garden";

        // Re usa a mesma curva geometrica, sem inversao da dianteira.
        // Na coleta, o intake fica na frente e MI trabalha continuamente.
        ElapsedTime timer = new ElapsedTime();
        double ultimoX = estimX;
        double instanteUltimoMovimento = 0;

        // Reserva compatível com a distancia ~1,2 m.
        double limiteTrecho = re ? 4.4 : 4.8;

        while (opModeIsActive()
            && !prazoEsgotado()
            && timer.seconds() < limiteTrecho) {

            atualizarOdom();

            double atual = timer.seconds();
            double destinoX = re
                ? X_GARDEN_RETORNO
                : X_GARDEN_FIM;
            double restante = Math.abs(destinoX - estimX);

            if (restante <= 0.013) {
                parar();
                atualizarOdom();
                telemetria(re
                    ? "Garden: retorno concluido"
                    : "Garden: coleta concluida; conferir objetos");
                return true;
            }

            // Verificacao geometrica usa o CORPO todo, nao so a pose.
            double inferior = folgaInferior();
            double esquerda = folgaEsquerda();
            double direita = ARENA - estimX - CHASSI / 2.0;

            if (inferior < FOLGA_MIN_GARDEN
                || esquerda < FOLGA_MIN_GARDEN
                || direita < FOLGA_MIN_GARDEN) {
                return falha(
                    "GARDEN GEOMETRIA",
                    "Parede proxima: inferior=" + inferior
                        + " esquerda=" + esquerda
                );
            }

            // Pure-pursuit: mira pouco a frente da pose atual numa
            // curva suave X->Y. O mesmo algoritmo funciona em marcha re.
            double dxVista = re ? 0.11 : -0.11;
            double xVista = Range.clip(
                estimX + dxVista,
                X_GARDEN_FIM,
                X_START
            );
            double yVista = linhaGardenY(xVista);

            double dx = xVista - estimX;
            double dy = yVista - estimY;

            double alvoYaw = norm(Math.toDegrees(
                Math.atan2(re ? dx : -dx,
                           re ? -dy : dy)
            ));

            double erroYaw = Range.clip(
                norm(alvoYaw - yaw()), -14.0, 14.0
            );
            double correcao = Range.clip(
                erroYaw * 0.019,
                -0.19, 0.19
            );

            // Frenagem continua e sem nova parada intermediaria.
            double rampa = Range.clip(0.22 + atual * 1.6,
                                     0.22, 1.0);
            double freio = Math.sqrt(
                2.0 * 1.0 * Math.max(restante, 0.0)
            );
            double teto = re ? 0.75 : 0.67;
            double v = Math.min(teto, Math.min(rampa,
                         Math.max(0.14, freio)));

            // Reduz velocidade junto da parede ou em desvio de rumo.
            if (inferior < 0.027 || Math.abs(erroYaw) > 7.0) {
                v = Math.min(v, 0.34);
            }

            double base = (re ? -1.0 : 1.0) * v;
            ME.setPower(Range.clip(base - correcao, -1, 1));
            MD.setPower(Range.clip(base + correcao, -1, 1));

            if (Math.abs(estimX - ultimoX) >= 0.014) {
                ultimoX = estimX;
                instanteUltimoMovimento = atual;
            }
            if (atual > 0.8
                && atual - instanteUltimoMovimento > 0.70) {
                return falha("GARDEN", "Sem progresso na coleta/recuo");
            }

            telemetry.addData("Garden centro", "%.3f, %.3f", estimX, estimY);
            telemetry.addData("Garden linha Y", "%.3f", linhaGardenY(estimX));
            telemetry.addData("Folga inferior", "%.3f", inferior);
            telemetry.addData("Garden yaw", "%.1f", yaw());
            telemetry.update();
            idle();
        }

        return falha("GARDEN",
            prazoEsgotado() ? "Tempo global" : "Timeout coleta/retorno");
    }

    // ==========================================
    // CONTROLE PD DE GIRO
    // ==========================================

    private boolean girar(double alvo) {

        parar();

        ElapsedTime timer = new ElapsedTime();

        double erroAnterior = norm(alvo - yaw());
        double ultimoAngulo = yaw();

        double ultimoMovimento = 0;
        double tempoEstavel = -1;

        while (
            opModeIsActive()
            && !prazoEsgotado()
            && timer.seconds() < 3.8
        ) {

            double agora = timer.seconds();
            double angulo = yaw();

            atualizarOdom();

            double erro = norm(alvo - angulo);

            if (Math.abs(erro) < TOL_GRAUS) {

                parar();

                if (tempoEstavel < 0)
                    tempoEstavel = agora;

                if (agora - tempoEstavel >= 0.08)
                    return true;

                idle();
                continue;
            }

            tempoEstavel = -1;

            double deriv =
                (erro - erroAnterior) / 0.02;

            erroAnterior = erro;

            double comando = Range.clip(
                0.022 * erro + 0.00065 * deriv,
                -0.70,
                0.70
            );

            if (Math.abs(comando) < 0.095) {

                comando = Math.copySign(
                    0.095,
                    erro
                );
            }

            ME.setPower(-comando);
            MD.setPower(comando);

            if (
                Math.abs(
                    norm(angulo - ultimoAngulo)
                ) >= 0.7
            ) {

                ultimoAngulo = angulo;
                ultimoMovimento = agora;
            }

            if (
                agora > 0.7
                && agora - ultimoMovimento > 0.72
            ) {

                return falha(
                    "IMU",
                    "Sem progresso angular"
                );
            }

            idle();
        }

        return falha(
            "GIRO",
            prazoEsgotado()
                ? "Prazo global esgotado"
                : "Timeout de giro"
        );
    }

    // ==========================================
    // CONTROLE DE DESLOCAMENTO
    // ==========================================

    private boolean andar(
        double alvoX,
        double alvoY,
        double metros,
        boolean re,
        double maxP
    ) {

        return andar(
            alvoX,
            alvoY,
            metros,
            re,
            maxP,
            0.026
        );
    }

    private boolean andar(
        double alvoX,
        double alvoY,
        double metros,
        boolean re,
        double maxP,
        double toleranciaFinal
    ) {

        int sinal = re ? -1 : 1;

        int inicialE = ME.getCurrentPosition();
        int inicialD = MD.getCurrentPosition();

        double ultimoTempoE = 0;
        double ultimoTempoD = 0;

        double ultimoAvancoE = 0;
        double ultimoAvancoD = 0;

        ElapsedTime timer = new ElapsedTime();

        double limite = Math.max(
            2.0,
            Math.min(
                10.0,
                2.3 + metros / 0.48
            )
        );

        while (
            opModeIsActive()
            && !prazoEsgotado()
            && timer.seconds() < limite
        ) {

            double agora = timer.seconds();

            atualizarOdom();

            int posE = ME.getCurrentPosition();
            int posD = MD.getCurrentPosition();

            double progressoE =
                sinal * (posE - inicialE) / TICKS_M;

            double progressoD =
                sinal * (posD - inicialD) / TICKS_M;

            double progresso =
                (progressoE + progressoD) / 2.0;

            double faltax = alvoX - estimX;
            double faltay = alvoY - estimY;

            double falta = Math.hypot(
                faltax,
                faltay
            );

            if (falta <= toleranciaFinal) {

                parar();
                return true;
            }

            if (progresso > metros + 0.20) {

                return falha(
                    "ODOMETRIA",
                    "Distancia excessiva"
                );
            }

            if (progresso < -0.025) {

                return falha(
                    "ENCODER",
                    "Sentido incorreto"
                );
            }

            double acelerar =
                0.25 + agora * 2.0;

            double frear = Math.sqrt(
                2.0 * 1.00 * (falta + 0.006)
            );

            double base = sinal * Math.min(
                maxP,
                Math.min(
                    acelerar,
                    Math.max(0.15, frear)
                )
            );

            double rumoAlvo = norm(
                Math.toDegrees(
                    Math.atan2(
                        re ? faltax : -faltax,
                        re ? -faltay : faltay
                    )
                )
            );

            double erroYaw = norm(
                rumoAlvo - yaw()
            );

            erroYaw = Range.clip(
                erroYaw,
                -22,
                22
            );

            double correcao = Range.clip(
                erroYaw * 0.021,
                -0.22,
                0.22
            );

            ME.setPower(
                Range.clip(
                    base - correcao,
                    -1,
                    1
                )
            );

            MD.setPower(
                Range.clip(
                    base + correcao,
                    -1,
                    1
                )
            );

            if (
                progressoE - ultimoAvancoE > 0.014
            ) {

                ultimoAvancoE = progressoE;
                ultimoTempoE = agora;
            }

            if (
                progressoD - ultimoAvancoD > 0.014
            ) {

                ultimoAvancoD = progressoD;
                ultimoTempoD = agora;
            }

            if (
                Math.abs(base) > 0.2
                && falta > 0.075
            ) {

                if (
                    agora - ultimoTempoE > 0.75
                    || agora - ultimoTempoD > 0.75
                ) {

                    return falha(
                        "ENCODER",
                        "Roda sem progresso"
                    );
                }
            }

            idle();
        }

        return falha(
            "DESLOCAMENTO",
            prazoEsgotado()
                ? "Prazo global esgotado"
                : "Timeout do trecho"
        );
    }

    // ==========================================
    // ODOMETRIA ENCODER + IMU
    // ==========================================

    private void atualizarOdom() {

        int e = ME.getCurrentPosition();
        int d = MD.getCurrentPosition();

        double ang = yaw();

        double difE =
            (e - odomEncoderE) / TICKS_M;

        double difD =
            (d - odomEncoderD) / TICKS_M;

        double ds = (difE + difD) / 2.0;

        yawDosEncoders += Math.toDegrees(
            (difD - difE) / 0.45
        );

        double meio =
            odomYaw
            + norm(ang - odomYaw) / 2.0;

        estimX +=
            -Math.sin(Math.toRadians(meio)) * ds;

        estimY +=
            Math.cos(Math.toRadians(meio)) * ds;

        odomEncoderE = e;
        odomEncoderD = d;

        odomYaw = ang;
    }

    // ==========================================
    // AUXILIARES
    // ==========================================

    private boolean prazoEsgotado() {

        return global != null
            && global.seconds() >= LIM_GLOBAL;
    }

    private double yaw() {

        return imu.getRobotYawPitchRollAngles()
            .getYaw(AngleUnit.DEGREES);
    }

    private static double norm(double a) {

        while (a > 180.0)
            a -= 360.0;

        while (a < -180.0)
            a += 360.0;

        return a;
    }

    private void parar() {

        if (ME != null)
            ME.setPower(0);

        if (MD != null)
            MD.setPower(0);
    }

    private boolean falha(
        String codigo,
        String descricao
    ) {

        motivoFalha = codigo + ": " + descricao;

        parar();

        telemetria("FALHA: " + motivoFalha);

        return false;
    }

    private void telemetria(String mensagem) {

        telemetry.addData(
            "Anterior",
            etapaAnterior
        );

        telemetry.addData(
            "Atual",
            etapaAtual
        );

        telemetry.addData(
            "Proxima",
            proximaEtapa
        );

        telemetry.addData(
            "Tempo",
            global == null ? 0 : global.seconds()
        );

        telemetry.addData(
            "X estimado",
            estimX
        );

        telemetry.addData(
            "Y estimado",
            estimY
        );

        telemetry.addData(
            "Yaw IMU",
            imu == null ? 0 : yaw()
        );

        telemetry.addData(
            "MS RPM",
            rpmMS
        );

        telemetry.addData(
            "RPM alvo",
            RPM_ALVO_MS_SIM
        );

        telemetry.addData(
            "MS power",
            potenciaMSComandada
        );

        telemetry.addData(
            "Shooter estado",
            estadoShooter
        );

        telemetry.addData(
            "RPM estavel",
            amostrasRpmEstaveis + "/"
                + AMOSTRAS_RPM_ESTAVEL
        );

        telemetry.addData(
            "RPM antes/min recuperacao",
            rpmAntesDisparo + " / "
                + (Double.isNaN(rpmMinRecuperacao)
                    ? "--"
                    : rpmMinRecuperacao)
        );

        telemetry.addData(
            "Encoder MS",
            MS == null ? 0 : MS.getCurrentPosition()
        );

        telemetry.addData(
            "Pulsos L1",
            pulsosL1 + " / 4"
        );

        telemetry.addData(
            "PARK alvo",
            X_PARK + ", " + Y_PARK
        );

        telemetry.addData(
            "Estado",
            mensagem
        );

        telemetry.update();
    }
}