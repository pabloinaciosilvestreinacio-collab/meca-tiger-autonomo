
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
 * MECA TIGER FTC #32578
 * MEQUINHA - AUTONOMO L1-04 4+4+4 RPM
 *
 * VERSAO EXPERIMENTAL V2: GARDEN + L2 + FLOWER SUPERIOR + DISPARO L2 + PARK
 *
 * ALTERACOES:
 * 1. Garden: curva continua guiada e retorno em re, sem giro na parede.
 * 2. FLOWER superior apos os tiros L2; terceiro disparo proximo a L1.
 * 3. PARK em X=0.255 Y=2.380, conforme retangulo vermelho marcado.
 * 4. Aproximacao de L2 calculada pelo chassi,
 *    deixando aproximadamente 15 mm de folga segura.
 *
 * MANTIDO:
 * - 4 disparos L1
 * - Intake Garden
 * - 4 disparos L2, 4 disparos extras apos Flower (candidato nao validado)
 * - Controle proporcional de RPM
 * - Percurso e giros anteriores
 * - Rampas de aceleracao
 * - Odometria por encoders e IMU
 *
 * CONFIGURACAO MECA SIM:
 * X inicial = 1.530
 * Y inicial = 0.255
 * Yaw inicial = 0
 * Chassi = 0.45 x 0.45 m
 * Bitola = 0.45 m
 * Velocidade maxima = 1.0 m/s
 * Aceleracao = 1.8 m/s2
 *
 * Este codigo e destinado ao Meca Sim.
 * ALERTA: a rota adicional e longa; o limite de 29.3s pode ser insuficiente.
 * Nao aprovado em simulacao ou em robo fisico.
 * Sem sensor de entrada/saida de POLLEN, 4 pulsos nao provam 4 coletas.

 */

@Autonomous(
    name = "TESTE L1-04 Flower SUPERIOR v2",
    group = "Meca Tiger"
)
public class AutonomoL104FlowerSuperiorRPMv2
        extends LinearOpMode {

    // ==========================================
    // CONSTANTES DE LOCOMOCAO
    // ==========================================

    private static final double TICKS_M =
            560.0 / (Math.PI * 0.090);

    private static final double X_START = 1.530;
    private static final double Y_START = 0.255;

    // PARK marcado pelo operador: centro aproximado (0.255, 2.380).
    // Com yaw=90 e chassi de 45 cm, folga lateral teorica = 30 mm.
    // O ultimo trecho e horizontal, sem girar junto a parede.
    private static final double X_PRE_PARK = 0.520;
    private static final double Y_PRE_PARK = 2.380;
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


    // 30 segundos do autonomo, com uma pequena margem de seguranca.
    private static final double LIM_GLOBAL = 29.3;
    // FLOWER SUPERIOR, ao lado da posicao de lancamento L2.
    // Eixo Y cresce de baixo (0) para cima (3.660).
    // Na segunda gravacao v1 desceu para Y=0.69 por erro de referencia.
    // Nao existe necessidade de voltar ao lado inferior da arena.
    // Trajeto da coleta: sair da parede L2 -> esquerda -> apontar +Y
    // -> aproximar centro ate 3.398 -> recuar sem girar -> L2 novamente.
    private static final double X_FLOWER = 1.30;
    private static final double Y_FLOWER_PRE = 3.060;
    private static final double Y_FLOWER_COLETA = 3.398;
    private static final double X_LANCAMENTO_L2 = 1.574;
    // Centro para relancamento: mesmo Y da aproximacao L2 original.
    private static final double MARGEM_FOLGA = 0.015;
    // Tempo minimo recomendado para concluir estacionamento apos L2.
    private static final double RESERVA_PARK = 4.0;
    // Limite superior para iniciar o terceiro disparo; evita iniciar
    // quatro pulsos sem ter tempo para terminar a corrida.
    private static final double LIMITE_INICIO_TERCEIRO = 23.8;
    private static final double TOL_GRAUS = 1.5;

    // ==========================================
    // SHOOTER
    // ==========================================

    private static final double MS_SHOT = 0.60;
    private static final double MS_STANDBY = 0.30;
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
    // Para L2, menor intervalo minimo, SEM dispensar RPM estavel.
    private static final double INTERVALO_TIROS_L2 = 0.70;
    private static final double PULSO_ALIMENTADOR = 0.14;
    // Ultimo POLLEN de L2 recebia pouco tempo para atravessar o alimentador.
    private static final double PULSO_ULTIMO_L2 = 0.25;
    private static final double PRE_ALIMENTACAO_L2 = 0.07;
    private static final double TEMPO_LIMPEZA_FINAL_L2 = 0.12;

    private static final int POLLENS_POR_POSTO = 4;

    // ==========================================
    // CHASSI E PAREDE L2
    // ==========================================

    private static final double ARENA = 3.660;
    private static final double CHASSI = 0.450;

    // Folga segura para impedir contato transitorio na parede de L2.
    private static final double FOLGA_L2 = 0.015;

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
    private int pulsosL2 = 0;
    private int pulsosFlower = 0;
    private boolean flowerPercorrida = false;

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

            telemetry.addLine("MECA TIGER - 4+4 RPM");
            telemetry.addLine("PARK VERMELHO");
            telemetry.addLine("L2 COM APROXIMACAO DA PAREDE");
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

            MS.setPower(MS_STANDBY);
            potenciaMSComandada = MS_STANDBY;

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
            // 5 - VOLTA PELO MESMO CORREDOR EM RE
            //     SEM GIRO JUNTO A PAREDE
            // ======================================

            // Preaquecer durante o recuo para economizar tempo em L2.
            MS.setPower(MS_SHOT);
            potenciaMSComandada = MS_SHOT;
            if (!percorrerGardenGuiado(true)) return;

            // ======================================
            // 6 - RETORNO HIVE
            // ======================================

            if (!segmento(
                "Garden -> corredor HIVE",
                "Corredor amarelo",
                1.574,
                0.550,
                true,
                0.95
            )) return;

            // ======================================
            // 7 - CORREDOR HIVE -> L2
            // ======================================

            if (!segmento(
                "Sob HIVE -> L2",
                "Aproximacao parede L2",
                1.574,
                3.340,
                true,
                0.85
            )) return;

            // ======================================
            // 8 - APROXIMACAO DA PAREDE L2
            // ======================================

            if (!aproximarL2())
                return;

            // ======================================
            // 9 - QUATRO DISPAROS EM L2
            // ======================================

            if (!dispararQuatro(
                "L2",
                LIM_GLOBAL - 3.60
            )) return;

            MS.setPower(0);
            MI.setPower(0);

            potenciaMSComandada = 0;
            estadoShooter = "DESLIGADO";

            SA.setPosition(SA_FECHADO);

            // ======================================
            // 10 - FLOWER SUPERIOR: PROXIMA A L2
            // ======================================
            // L2 esta no alto do campo. Sao necessarios apenas 30-40 cm
            // para liberar o giro, nao atravessar o HIVE ate Y=0.69.
            // PAREDES: chassi 45 cm, sem giros a Y>3.34.
            if (!deslocarSemGirar(
                "Sair da parede superior L2", "Acesso Flower superior",
                0.36, false, 0.65
            )) return;

            // Curta translacao horizontal para a Flower a esquerda de L2.
            if (!segmento(
                "L2 -> lado esquerdo da Flower", "Apontar para Flower",
                X_FLOWER, Y_FLOWER_PRE,
                false, 0.53
            )) return;

            // Robô agora tem folga para girar e apontar rumo +Y.
            if (!girar(0.0)) return;

            etapaAnterior = etapaAtual;
            etapaAtual = "Coleta Flower superior em +Y";
            proximaEtapa = "Recuo em re Flower";
            MI.setPower(MI_INTAKE);
            try {
                if (!avancarFlowerComLimite()) return;
            } finally {
                MI.setPower(0);
            }
            flowerPercorrida = true;

            // Sai da parede superior de costas, sem tentar girar encostado.
            if (!deslocarSemGirar(
                "Recuar da Flower em re", "Regressar L2",
                0.35, true, 0.66
            )) return;

            // Reaquecer durante o retorno (sem disparar antes de RPM estavel).
            MS.setPower(MS_SHOT);
            potenciaMSComandada = MS_SHOT;

            // Caso o novo percurso consuma tempo demais, preservar o PARK.
            // Esta condicao NAO significa que a missao 4+4+4 foi concluida.
            if (global.seconds() <= LIMITE_INICIO_TERCEIRO - 2.0) {

                if (!segmento(
                    "Flower -> faixa de L2", "Aproximacao terceiro disparo",
                    X_LANCAMENTO_L2, Y_FLOWER_PRE,
                    false, 0.64
                )) return;

                // Mesma posicao e orientacao do L2 ja testado.
                if (!segmento(
                    "L2 novamente: alinhar e aproximar", "Disparo extra Flower",
                    X_LANCAMENTO_L2,
                    ARENA - CHASSI / 2.0 - FOLGA_L2,
                    true, 0.40
                )) return;

                if (global.seconds() <= LIMITE_INICIO_TERCEIRO) {
                    if (!dispararQuatro(
                        "FLOWER", LIM_GLOBAL - RESERVA_PARK
                    )) return;
                } else {
                    motivoFalha = "Terceiro disparo omitido: tempo insuficiente";
                    telemetria(motivoFalha);
                }
            } else {
                motivoFalha = "Terceiro disparo omitido: rota excedeu reserva";
                telemetria(motivoFalha);
            }

            MS.setPower(0);
            potenciaMSComandada = 0;
            estadoShooter = "DESLIGADO";
            MI.setPower(0);
            SA.setPosition(SA_FECHADO);

            // ======================================
            // 11 - PARK MARCADO: ROTA DIRETA PELO LADO ESQUERDO
            // ======================================
            // Nao tenta realizar a antiga excursao para Y=0.69.
            // Deixa a posicao atual e aponta para corredor esquerdo.
            // So iniciar giro depois de se afastar da parede superior.
            if (estimY > 3.20) {
                if (!girar(180.0)) return;
                if (!deslocarSemGirar(
                    "Saida segura antes do PARK", "Pre-PARK",
                    estimY - 3.05, false, 0.65
                )) return;
            }

            if (!segmento(
                "Saida L2/Flower -> pre-PARK", "PARK lateral vermelho",
                X_PRE_PARK, Y_PRE_PARK,
                false, 0.88
            )) return;

            if (!segmento(
                "Pre-PARK -> PARK vermelho", "Fim L1-04",
                X_PARK, Y_PARK,
                false, 0.60
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
            boolean completos = pulsosL1 == POLLENS_POR_POSTO
                && pulsosL2 == POLLENS_POR_POSTO
                && pulsosFlower == POLLENS_POR_POSTO;
            etapaAtual = completos
                ? "L1-04 4+4+4 CONCLUIDO NOMINALMENTE"
                : "PARK FINALIZADO; 3o LANCAMENTO INCOMPLETO";
            proximaEtapa = "Conferir tiros, coleta e relatorio";
            telemetria(completos
                ? "4+4+4 pulsos e PARK; conferir pontuacao"
                : "L1-04 PARCIAL; nao declarar missao 12 tiros concluida"
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

        proximaEtapa = posto.equals("L1")
            ? "Saida reta de L1"
            : posto.equals("L2")
                ? "Saida reta L2"
                : "PARK vermelho";

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

            boolean emL2 = posto.equals("L2");
            boolean terceiroPosto = posto.equals("FLOWER");
            double primeiroPermitido =
                tiro == 1
                    ? (emL2 || terceiroPosto ? global.seconds() : 0.82)
                    : inicioAnterior + ((emL2 || terceiroPosto)
                        ? INTERVALO_TIROS_L2
                        : INTERVALO_TIROS);

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
                (emL2 || terceiroPosto) ? PRE_ALIMENTACAO_L2 : 0.025,
                prazo
            )) {
                MI.setPower(0);
                return falha("SHOOTER", "Pre-alimentacao expirada: " + posto + " tiro " + tiro);
            }

            SA.setPosition(SA_ABERTO);
            rpmAntesDisparo = rpmMS;
            inicioAnterior = global.seconds();

            double pulso = ((emL2 || terceiroPosto) && tiro == POLLENS_POR_POSTO)
                ? PULSO_ULTIMO_L2
                : PULSO_ALIMENTADOR;

            if (!esperarAte(
                pulso,
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
            if (emL2) pulsosL2 = feitos;
            else if (terceiroPosto) pulsosFlower = feitos;
            else pulsosL1 = feitos;

            // Somente no ultimo L2: manter MS girando enquanto o ultimo
            // POLLEN termina de sair. O robô não inicia PARK antes disso.
            if ((emL2 || terceiroPosto) && tiro == POLLENS_POR_POSTO) {
                estadoShooter = "ESVAZIAMENTO_FINAL_L2";
                if (!esperarAte(TEMPO_LIMPEZA_FINAL_L2, prazo)) {
                    return falha("SHOOTER", posto + ": ultimo pulso sem tempo de finalizacao");
                }
            }

            telemetria(
                posto + ": " + feitos
                    + "/4; RPM=" + rpmMS
            );
        }

        if (posto.equals("L1")) pulsosL1 = feitos;
        else if (posto.equals("L2")) pulsosL2 = feitos;
        else if (posto.equals("FLOWER")) pulsosFlower = feitos;

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
    // COLETA FLOWER DO LADO SUPERIOR DA ARENA
    // ==========================================

    private boolean avancarFlowerComLimite() {
        // Frente precisa apontar para +Y (topo da arena), yaw=0.
        // A v1 usava yaw=180 e Y=0.278, ambos invertidos.
        atualizarOdom();
        if (Math.abs(norm(yaw())) > 4.0) {
            return falha("FLOWER", "Frente nao aponta +Y para Flower superior");
        }

        double distancia = Y_FLOWER_COLETA - estimY;
        if (distancia < -0.010 || distancia > 0.60) {
            return falha("FLOWER", "Aproximacao superior invalida: " + distancia);
        }

        double folgaFinalTeorica =
            ARENA - (Y_FLOWER_COLETA + CHASSI / 2.0);
        if (folgaFinalTeorica < MARGEM_FOLGA) {
            return falha("FLOWER", "Alvo invade parede superior");
        }

        if (distancia > 0.012) {
            if (!andar(
                estimX, Y_FLOWER_COLETA,
                distancia, false, 0.36, 0.012
            )) return false;
        }

        parar();
        atualizarOdom();
        double folga = ARENA - (estimY + CHASSI / 2.0);
        if (folga < MARGEM_FOLGA) {
            return falha("FLOWER", "Folga de parede superior insuficiente: " + folga);
        }
        telemetria("Flower superior percorrida; confirmar 4 POLLEN visualmente");
        return true;
    }

    // ==========================================
    // APROXIMACAO L2 - ALTERACAO PRINCIPAL
    // ==========================================

    private boolean aproximarL2() {

        etapaAnterior = etapaAtual;
        etapaAtual = "Aproximacao parede L2";
        proximaEtapa = "Disparo 4 POLLEN L2";

        parar();

        for (
            int i = 0;
            i < 35 && opModeIsActive();
            i++
        ) {
            idle();
            atualizarOdom();
        }

        /*
         * O robo deve chegar com a frente
         * apontada para o HIVE e a traseira
         * apontada para a parede superior.
         *
         * Antes de qualquer giro, verifica
         * a folga para evitar colisao.
         */

        double angAtual = Math.toRadians(yaw());

        double meiaYAtual = (CHASSI / 2.0) * (
            Math.abs(Math.sin(angAtual))
            + Math.abs(Math.cos(angAtual))
        );

        double folgaAtual =
            ARENA - (estimY + meiaYAtual);

        if (folgaAtual < 0.028) {

            return falha(
                "L2",
                "Sem folga segura para alinhar"
            );
        }

        if (Math.abs(norm(180.0 - yaw())) > 1.5) {

            if (!girar(180.0))
                return false;

            parar();

            for (
                int i = 0;
                i < 4 && opModeIsActive();
                i++
            ) {
                idle();
                atualizarOdom();
            }
        }

        double erroYaw = Math.abs(
            norm(180.0 - yaw())
        );

        if (erroYaw > 1.5) {

            return falha(
                "L2",
                "Rumo incorreto para parede"
            );
        }

        /*
         * Calcula o limite do chassi
         * considerando o angulo real da IMU.
         *
         * O alvo e ficar 2 mm antes da parede.
         *
         * Assim o robo chega visualmente
         * encostado sem ultrapassar o limite
         * geometrico da arena.
         */

        double ang = Math.toRadians(yaw());

        double meiaY = (CHASSI / 2.0) * (
            Math.abs(Math.sin(ang))
            + Math.abs(Math.cos(ang))
        );

        double yAlvo =
            ARENA - meiaY - FOLGA_L2;

        double distancia =
            yAlvo - estimY;

        telemetry.addData("L2 Y alvo", yAlvo);
        telemetry.addData("L2 distancia", distancia);
        telemetry.update();

        if (distancia < -0.003 || distancia > 0.14) {

            return falha(
                "L2",
                "Distancia inesperada: " + distancia
            );
        }

        if (distancia > 0.002) {

            /*
             * Aproximacao final em baixa potencia.
             *
             * Tolerancia de 1,5 mm.
             * Sem impacto proposital.
             */

            if (!andar(
                estimX,
                yAlvo,
                distancia,
                true,
                0.20,
                0.0015
            )) return false;
        }

        parar();

        for (
            int i = 0;
            i < 5 && opModeIsActive();
            i++
        ) {
            idle();
            atualizarOdom();
        }

        double angFinal = Math.toRadians(yaw());

        double meiaYFinal = (CHASSI / 2.0) * (
            Math.abs(Math.sin(angFinal))
            + Math.abs(Math.cos(angFinal))
        );

        double folgaFinal =
            ARENA - (estimY + meiaYFinal);

        telemetry.addData(
            "L2 folga final (m)",
            folgaFinal
        );

        telemetry.addData(
            "L2 Y estimado",
            estimY
        );

        telemetry.update();

        /*
         * Nao aceita penetracao estimada
         * na parede nem afastamento excessivo.
         */

        if (
            folgaFinal < 0.005
            || folgaFinal > 0.028
        ) {

            return falha(
                "L2",
                "Folga final fora da faixa: "
                    + folgaFinal
            );
        }

        telemetria(
            "L2 alinhado proximo da parede"
        );

        return true;
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
            "Pulsos L1/L2",
            pulsosL1 + " / " + pulsosL2
        );

        telemetry.addData("Pulsos Flower", pulsosFlower);
        telemetry.addData("Flower percorrida", flowerPercorrida);
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