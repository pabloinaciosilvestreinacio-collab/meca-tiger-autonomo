
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
 * MEQUINHA - AUTONOMO L1-05 4+4+4 RPM
 *
 * VERSAO EXPERIMENTAL L1-05 V12: 4 TIROS L2/FLOWER + RPM + CURVAS V5
 *
 * ALTERACOES:
 * 1. Garden: curva continua guiada e retorno em re, sem giro na parede.
 * 2. FLOWER superior apos os tiros L2; retorno obrigatorio a L2 para terceira sequencia.
 * 3. PARK em X=0.255 Y=2.380, conforme retangulo vermelho marcado.
 * V12: teste de TIMEOUT L2 ampliado, preservando 18mm de tolerancia.
 * V10 EXPERIMENTAL: emendar retorno do Garden com a curva longa L2
 * sem frear ate zero e sem parar os motores na transicao.
 * 4. Aproximacao de L2 calculada pelo chassi,
 *    deixando aproximadamente 15 mm de folga segura.
 *
 * MANTIDO:
 * - 4 disparos L1
 * - Intake Garden
 * - v7: intertravamento MI/MS para evitar descarga em coleta
 * - v7: permissao de feed exige pose de L1/L2 + RPM estavel
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
 * Nova versao ainda nao aprovada em simulacao ou em robo fisico.
 * Sem sensor de entrada/saida de POLLEN, 4 pulsos nao provam 4 coletas.

 */

@Autonomous(
    name = "TESTE L1-05 V12 L2 TIMEOUT",
    group = "Meca Tiger"
)
public class AutonomoL105GardenL2TimeoutMaiorRPMv12
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
    // Centro com a traseira a ~15 mm da parede superior ao yaw=180.
    // A velocidade diminui continuamente ate esta coordenada.
    private static final double Y_L2_LANCAMENTO = 3.420;
    // Mesmo ponto de PRE-APROXIMACAO que funcionou no primeiro L2.
    // Em Y=3.340 restam 95 mm entre chassi e parede superior;
    // o metodo aproximarL2 pode executar o ajuste fino com seguranca.
    private static final double Y_PRE_APROX_L2 = 3.340;
    // Centro para relancamento: mesmo Y da aproximacao L2 original.
    private static final double MARGEM_FOLGA = 0.015;
    // Reserva experimental de tempo para o PARK (nao garantida).
    // Estimada pela velocidade livre medida no corredor, com limites;
    // nunca utiliza todo o tempo de 29.3 s so para o shooter.
    private static final double RESERVA_PARK_MIN = 2.10;
    private static final double RESERVA_PARK_MAX = 3.10;
    private static final double VELOCIDADE_RETA_REFERENCIA = 0.82;
    // Acesso Flower em curva continua com potencia moderada.
    private static final double P_ENTRADA_FLOWER_CURVA = 0.50;
    // Hora de alerta para diagnostico do terceiro disparo.
    // Nao omite a etapa: o limite global ainda impede disparos atrasados.
    private static final double LIMITE_INICIO_TERCEIRO = 23.8; // Referencia de diagnostico; nao omite tiro
    private static final double TOL_GRAUS = 1.5;

    // Velocidades especificas para corredores livres.
    // Potencia nao corresponde diretamente a m/s.
    // Aprox. das paredes e coleta continuam lentas.
    private static final double P_CORREDOR_HIVE = 0.96;
    private static final double P_SAIDA_L2 = 0.72;
    private static final double P_TRANSVERSAL_FLOWER = 0.73;
    private static final double P_RECUO_FLOWER = 0.78;
    private static final double P_RETORNO_L2 = 0.77;
    private static final double P_APROX_FINAL_L2 = 0.40;
    private static final double P_PARK_LIVRE = 0.94;
    private static final double P_PARK_PAREDE = 0.60;

    // ==========================================
    // SHOOTER
    // ==========================================

    private static final double MS_SHOT = 0.60;
    private static final double MS_STANDBY = 0.30;
    private static final double MS_COLETA_SEGURO = 0.00; // v7: evita disparo em coleta
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
    // Para L2 e FLOWER, intervalo de alimentacao seguro no teste.
    // V5: L1=0.82s (4 tiros), L2=0.70s (3 tiros), FLOWER=0.44s (3 tiros).
    // Aumentar o intervalo de alimentacao para evitar pulso anterior ao
    // rearme mecanico/simulador. O RPM continua sendo intertravamento EXTRA.
    // Estes valores sao experimentais; confirmar no Meca Sim.
    private static final double INTERVALO_TIROS_L2 = 0.84;
    private static final double INTERVALO_TIROS_TERCEIRO = 0.84;
    private static final double PULSO_ALIMENTADOR = 0.14;
    // Ultimo POLLEN de L2 recebia pouco tempo para atravessar o alimentador.
    // O quarto POLLEN pode chegar atrasado: manter servo/MI por mais tempo.
    // Nao equivale a confirmar fisicamente a passagem da bola.
    private static final double PULSO_ULTIMO_L2 = 0.32;
    private static final double PRE_ALIMENTACAO_L2 = 0.09;
    private static final double TEMPO_LIMPEZA_FINAL_L2 = 0.16;

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
    // Contador de PULSOS, nao sensor de POLLEN. Mantem historico por posto.
    private double ultimoPulsoL2 = -1.0;
    private double ultimoPulsoFlower = -1.0;
    private double intervaloEfetivoL2 = -1.0;
    private double intervaloEfetivoFlower = -1.0;
    private String ultimoEventoFeed = "SEM PULSOS";
    private double chegadaTerceiroL2 = -1.0;
    private double deadlineTerceiro = -1.0;
    private double primeiroPulsoTerceiro = -1.0;
    private double ultimoPulsoTerceiro = -1.0;
    private String diagnosticoTerceiro = "NAO INICIADO";
    private boolean flowerPercorrida = false;

    // -------- MONITOR DE RPM CONTINUO --------
    // Amostras em TODOS os loops, inclusive curvas/esperas;
    // somente quando controleAtivo == true.
    private boolean controleShooterAtivo = false;
    // Estados explicitos do mecanismo: nunca permitir intake e shooter >50%
    // ao mesmo tempo fora da janela autorizada de alimentacao em L1/L2.
    private enum ModoMecanismo {
        PARADO, COLETA_GARDEN, COLETA_FLOWER,
        TRANSITO, DISPARO_L1, DISPARO_L2, DISPARO_EXTRA, PARK
    }
    private ModoMecanismo modoMecanismo = ModoMecanismo.PARADO;
    private boolean alimentacaoAutorizada = false;
    private int intertravamentos = 0;
    private int recusasPose = 0;
    private String ultimoBloqueio = "NENHUM";
    private boolean encoderShooterFalhou = false;
    private boolean rpmAmostraValida = false;
    private int encoderMSAnterior;
    private int encoderMSAoAtivar;
    private int encoderMSObservado;
    private double instanteMSAtivado = 0;
    private double instanteAmostraMS = 0;
    private double instanteUltimaMudancaMS = 0;
    private double instanteUltimaAmostraValida = -1;

    // -------- MEDICAO DE TEMPOS POR FASE --------
    // Indices: 0 L1, 1 Garden, 2 primeiro L2, 3 Flower,
    // 4 terceiro L2, 5 PARK.
    private final String[] nomesFases = {
        "L1 4 tiros", "Garden", "L2 primeiro", "Flower",
        "L2 terceiro", "PARK"
    };
    private final double[] temposFases = {
        -1, -1, -1, -1, -1, -1
    };
    private double inicioFase = 0;
    private double reservaParkCalculada = -1;
    // Velocidade medida em trechos seguros, filtrada.
    private double velocidadeRetaMedida = Double.NaN;
    private double referenciaVelX = Double.NaN;
    private double referenciaVelY = Double.NaN;
    private double referenciaVelT = 0;


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

            ativarControleShooter();
            if (!dispararQuatro("L1", 4.20))
                return;

            desativarControleShooter(MS_STANDBY);
            marcarFase(0); // L1 finalizado

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
                atualizarRPMContinuo();
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

            // COLETA: shooter desligado (0.00) enquanto MI=1.00.
            // O Meca Sim pode ejetar POLLEN com MI>0.50 e MS>0.50,
            // mesmo com SA fechado. Nao aquecer durante a coleta!
            iniciarColetaSegura(ModoMecanismo.COLETA_GARDEN);
            try {
                if (!percorrerGardenGuiado(false)) return;
                if (!percorrerGardenGuiado(true)) return;
            } finally {
                encerrarColetaSegura();
            }
            // v10: motores ainda em re ao desligar MI; nao chamar parar().
            // Com a curva seguinte, constitui UM movimento sem waypoint de parada.
            telemetry.addData("Garden-L2 transicao sem STOP", "%.2f s", global.seconds());
            telemetry.addData("Garden-L2 mot E/D", "%.2f/%.2f", ME.getPower(), MD.getPower());
            telemetry.update();
            // MS acelera durante o longo percurso Garden -> L2,
            // quando MI esta OFF. Nao ha espera extra.
            modoMecanismo = ModoMecanismo.TRANSITO;
            ativarControleShooter();
            marcarFase(1); // Garden + retorno

            // ======================================
            // 6-8 - CURVA CONTINUA DO GARDEN A L2
            // ======================================
            // A frente sai do Garden apontando para a esquerda (yaw~90).
            // O robo marcha em re e curva progressivamente para yaw~180,
            // depois segue reto ate L2. Nenhum giro parado no corredor.
            // Tambem integra a aproximacao FINAL de L2, com desaceleracao.
            // O ponto 1.574/0.550 foi ELIMINADO como parada obrigatoria.

            if (!seguirBezier(
                "Garden -> L2 em curva continua",
                "4 disparos em L2",
                true,
                estimX + 0.410, estimY,
                X_LANCAMENTO_L2, 1.300,
                X_LANCAMENTO_L2, Y_L2_LANCAMENTO,
                P_CORREDOR_HIVE, 180.0, 7.0, 10, 0.018
            )) return;

            if (!confirmarPoseL2("L2 PRIMEIRO")) return;

            // ======================================
            // 9 - QUATRO DISPAROS EM L2
            // ======================================

            if (!dispararQuatro(
                "L2",
                LIM_GLOBAL - 3.60
            )) return;
            marcarFase(2); // primeiro L2

            // Mantem o MS na potencia-base de tiro durante a Flower.
            // A alimentacao fica BLOQUEADA (MI=0 e SA fechado).
            // Isso economiza spin-up antes do terceiro ciclo; a liberacao
            // segue dependendo das tres amostras consecutivas de RPM.
            // Preparo do shooter permitido apenas ate chegar a Flower.
            // Antes de iniciar intake, iniciarColetaSegura() aplica standby.
            estadoShooter = "RPM_CONTINUO_ACESSO_FLOWER";
            MI.setPower(0);
            SA.setPosition(SA_FECHADO);
            modoMecanismo = ModoMecanismo.TRANSITO;

            // ======================================
            // 10 - FLOWER SUPERIOR AO LADO DE L2
            // ======================================
            // Em vez de afastar/reorientar/parar tres vezes, executar
            // um unico arco continuo com tangentes de entrada e saida.
            // Curva de entrada na Flower: a tangente inicial vai para -Y
            // (yaw 180), a final vai para +Y (yaw 0). A curva inteira
            // permanece acima do HIVE (Y >= 2.75 no traçado teórico).
            // Nao existe parada intermediaria para girar 90/0 graus.
            if (!entrarFlowerEmCurva()) return;

            etapaAnterior = etapaAtual;
            etapaAtual = "Coleta da Flower superior em +Y";
            proximaEtapa = "Voltar em re para L2";
            // A mesma protecao vale para a Flower: nao usar MS>50%
            // enquanto MI puxa os POLLEN. Intake permanece no recuo.
            iniciarColetaSegura(ModoMecanismo.COLETA_FLOWER);
            try {
                if (!avancarFlowerComLimite()) return;
                flowerPercorrida = true;
                if (!deslocarSemGirar(
                    "Saida segura Flower em re", "Voltar para L2",
                    0.35, true, P_RECUO_FLOWER
                )) return;
            } finally {
                encerrarColetaSegura();
            }
            marcarFase(3); // Flower + recuo

            // Aquecer MS SOMENTE depois de desligar MI; RPM se estabiliza
            // durante o retorno em curva, sem espera fixa.
            modoMecanismo = ModoMecanismo.TRANSITO;
            ativarControleShooter();
            estadoShooter = "RPM_CONTINUO_RETORNO_L2";

            // ======================================
            // 11 - UMA CURVA DE RETORNO FLOWER -> L2
            // ======================================
            // O unico giro parado necessario (yaw ~128) acontece LONGE
            // da parede superior. Depois o robo gira ENQUANTO recua:
            // elimina as paradas nos waypoints de Y=3.060 e Y=3.340.
            // Nao chamar aproximarL2() depois: a propria curva freia
            // e para na posicao final de tiro, conservando yaw=180.

            if (!girar(128.0)) return;

            if (!seguirBezier(
                "Flower -> L2 em curva continua",
                "Terceiro ciclo de 4 disparos",
                true,
                1.450, 3.180,
                1.520, 3.180,
                X_LANCAMENTO_L2, Y_L2_LANCAMENTO,
                P_RETORNO_L2, 180.0, 3.5, 7, 0.018
            )) return;

            if (!confirmarPoseL2("L2 RETORNO")) return;

            // ======================================
            // 12 - QUATRO NOVOS DISPAROS EM L2
            // ======================================
            // A sequencia extra e OBRIGATORIA no L1-05.
            // Se nao couber no tempo, registra falha; nunca diz que
            // concluiu a estrategia com disparos omitidos.
            if (global.seconds() > LIMITE_INICIO_TERCEIRO) {
                telemetria("ALERTA: L2 extra em t="+global.seconds()+"; reservar PARK");
            }
            // Se o RPM demorar e a reserva do PARK for atingida,
            // registrar os disparos incompletos, mas NAO abortar a rota
            // automaticamente: tentar completar o estacionamento.
            // Nunca anunciar 12 disparos se houve menos de 4 pulsos extras.
            chegadaTerceiroL2 = global.seconds();
            reservaParkCalculada = calcularReservaPark();
            deadlineTerceiro = LIM_GLOBAL - reservaParkCalculada;
            diagnosticoTerceiro = "L2 retorno em "
                + chegadaTerceiroL2 + "s, prazo final "
                + deadlineTerceiro + "s, reserva PARK="
                + reservaParkCalculada + "s";
            telemetria("3o ciclo: chegada="+chegadaTerceiroL2
                + "s; limite tiro="+deadlineTerceiro+"s");

            // Antes a v3 bloqueava a entrada apos 25.1 s, ainda que
            // restasse tempo para efetuar pulsos. O metodo abaixo
            // aproveita a janela expandida e nao pula o 3o posto.
            boolean terceiroCompleto = dispararQuatro(
                "FLOWER", deadlineTerceiro
            );
            if (!terceiroCompleto) {
                if (!opModeIsActive() || prazoEsgotado()) return;
                etapaAnterior = etapaAtual;
                etapaAtual = "Terceiro disparo parcial; fazer PARK";
                proximaEtapa = "Saida reta L2 -> PARK";
                diagnosticoTerceiro = "INCOMPLETO " + pulsosFlower
                    + "/4: " + motivoFalha;
                telemetria("ALERTA: 3o posto " + pulsosFlower
                    + "/4; motivo="+diagnosticoTerceiro
                    + "; tentar PARK antes do prazo global");
            }

            if (terceiroCompleto) {
                diagnosticoTerceiro = "4/4 pulsos comandados em "
                    + ultimoPulsoTerceiro + "s";
            }
            marcarFase(4); // terceiro L2, mesmo se incompleto
            desativarControleShooter(0);
            MI.setPower(0);
            SA.setPosition(SA_FECHADO);
            estadoShooter = "DESLIGADO";

            // ======================================
            // 13 - PARK EM UMA CURVA UNICA
            // ======================================
            // Sai de L2 para -Y (yaw=180), arqueia para a esquerda
            // longe do HIVE, termina apontando para -X (yaw=90).
            // A curva substitui: 33cm reto + giro + pre-PARK + giro.
            // Mesmo PARK marcado na versao aprovada L1-03.
            modoMecanismo = ModoMecanismo.PARK;
            if (!seguirBezier(
                "Saida L2 -> PARK sem paradas",
                "Fim L1-05",
                false,
                X_LANCAMENTO_L2, 2.650,
                0.620, 2.700,
                X_PARK, Y_PARK,
                P_PARK_LIVRE, 90.0, 5.2, 6, 0.035
            )) return;
            marcarFase(5); // PARK

            parar();

            for (
                int i = 0;
                i < 4 && opModeIsActive();
                i++
            ) {

                atualizarRPMContinuo();

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
                ? "L1-05 V12 4+4+4 CONCLUIDO NOMINALMENTE"
                : "PARK FINALIZADO; 3o LANCAMENTO INCOMPLETO";
            proximaEtapa = "Conferir tiros, coleta e relatorio";
            telemetria(completos
                ? "4+4+4 comandos de alimentacao; conferir POLLEN e PARK"
                : "L1-05 PARCIAL; nao declarar missao 12 tiros concluida"
            );

        } finally {

            parar();
            alimentacaoAutorizada = false;
            modoMecanismo = ModoMecanismo.PARADO;

            if (MI != null)
                MI.setPower(0);

            if (MS != null)
                MS.setPower(0);

            if (SA != null)
                SA.setPosition(SA_FECHADO);
            controleShooterAtivo = false;
            if (global != null && imu != null) {
                telemetria("FIM L1-05 v12: " + etapaAtual
                    + "; motivo=" + motivoFalha);
            }
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

        alimentarSomenteSeAutorizado(false);
        if (!validarPoseDisparo(posto)) return false;
        modoMecanismo = posto.equals("L1") ? ModoMecanismo.DISPARO_L1
            : posto.equals("L2") ? ModoMecanismo.DISPARO_L2
            : ModoMecanismo.DISPARO_EXTRA;

        // RPM ja e monitorado; nao reiniciar a janela no mesmo posto.
        if (!controleShooterAtivo) ativarControleShooter();

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
                    : inicioAnterior + (terceiroPosto
                        ? INTERVALO_TIROS_TERCEIRO
                        : emL2 ? INTERVALO_TIROS_L2
                        : INTERVALO_TIROS);

            if (!aguardarRpmEIntervalo(
                primeiroPermitido,
                prazo
            )) {
                if (terceiroPosto) {
                    diagnosticoTerceiro = "BLOQUEADO RPM/PRAZO: "
                        + feitos + "/4, t=" + global.seconds()
                        + ", limite=" + prazo + ", RPM=" + rpmMS
                        + ", estaveis=" + amostrasRpmEstaveis;
                }
                return falha(
                    "SHOOTER",
                    "RPM/tempo insuficiente: "
                        + feitos + "/4 em " + posto
                        + " t=" + global.seconds()
                        + " limite=" + prazo
                );
            }

            // Intertravamento verificado A CADA POLLEN: nunca alimentar
            // no trajeto, durante curva ou com o robo fora do posto.
            if (!validarPoseDisparo(posto)) return false;
            if (!rpmAmostraValida || amostrasRpmEstaveis < AMOSTRAS_RPM_ESTAVEL) {
                return falha("INTERTRAVA RPM", posto + " tiro " + tiro
                    + " perdeu estabilidade antes do feeder");
            }
            alimentacaoAutorizada = true;
            MI.setPower(MI_FEED);

            if (!esperarAte(
                (emL2 || terceiroPosto) ? PRE_ALIMENTACAO_L2 : 0.025,
                prazo
            )) {
                alimentarSomenteSeAutorizado(false);
                return falha("SHOOTER", "Pre-alimentacao expirada: " + posto + " tiro " + tiro);
            }

            SA.setPosition(SA_ABERTO);
            rpmAntesDisparo = rpmMS;
            double inicioNovoPulso = global.seconds();
            if (emL2) {
                if (ultimoPulsoL2 >= 0)
                    intervaloEfetivoL2 = inicioNovoPulso - ultimoPulsoL2;
                ultimoPulsoL2 = inicioNovoPulso;
            } else if (terceiroPosto) {
                if (ultimoPulsoFlower >= 0)
                    intervaloEfetivoFlower = inicioNovoPulso - ultimoPulsoFlower;
                ultimoPulsoFlower = inicioNovoPulso;
            }
            inicioAnterior = inicioNovoPulso;
            ultimoEventoFeed = posto + " tiro=" + tiro + "/4 t="
                + inicioNovoPulso + " RPM=" + rpmMS;

            double pulso = ((emL2 || terceiroPosto) && tiro == POLLENS_POR_POSTO)
                ? PULSO_ULTIMO_L2
                : PULSO_ALIMENTADOR;

            if (!esperarAte(
                pulso,
                prazo
            )) {

                alimentarSomenteSeAutorizado(false);
                return false;
            }

            alimentarSomenteSeAutorizado(false);
            if (terceiroPosto) {
                if (primeiroPulsoTerceiro < 0) {
                    primeiroPulsoTerceiro = inicioAnterior;
                }
                ultimoPulsoTerceiro = global.seconds();
                diagnosticoTerceiro = "ALIMENTACAO FLOWER "
                    + (feitos + 1) + "/4 t=" + ultimoPulsoTerceiro;
            }
            aguardandoRecuperacao = true;
            amostrasRpmEstaveis = 0; // nova estabilidade para proximo tiro
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
                posto + ": pulso " + feitos + "/4; RPM=" + rpmMS
                    + "; intervalo L2=" + intervaloEfetivoL2
                    + "; intervalo FLOWER=" + intervaloEfetivoFlower
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
    // ARVORE DE PERMISSOES DO MECANISMO (v7)
    // ==========================================

    // COLETAR (MS=0) -> retornar -> shooter SPINUP -> confirmar pose/RPM
    // -> alimentar -> esvaziar -> proximo ciclo/PARK.
    // Em Meca Sim, MI>0.5 e MS>0.5 provoca lancamento mesmo com SA fechado.
    private void iniciarColetaSegura(ModoMecanismo fase) {
        alimentarSomenteSeAutorizado(false);
        desativarControleShooter(MS_COLETA_SEGURO);
        modoMecanismo = fase;
        estadoShooter = "STANDBY_DURANTE_COLETA";
        MI.setPower(MI_INTAKE);
    }

    private void encerrarColetaSegura() {
        alimentarSomenteSeAutorizado(false);
        modoMecanismo = ModoMecanismo.TRANSITO;
    }

    private void alimentarSomenteSeAutorizado(boolean liberar) {
        if (MI != null) MI.setPower(0);
        if (SA != null) SA.setPosition(SA_FECHADO);
        alimentacaoAutorizada = liberar;
    }

    // Pose do centro estimada por encoders/IMU; nao comprova alvo visivel.
    // E uma intertrava contra lancamento em rota, nao um detector de acerto.
    private boolean validarPoseDisparo(String posto) {
        atualizarOdom();
        if (!opModeIsActive() || prazoEsgotado()) return false;
        if (Math.abs(ME.getPower()) > 0.06 ||
            Math.abs(MD.getPower()) > 0.06) {
            recusasPose++;
            ultimoBloqueio = "Robo nao esta parado: " + posto;
            return falha("INTERTRAVA POSE", ultimoBloqueio);
        }
        boolean l1 = posto.equals("L1");
        boolean localOk;
        if (l1) {
            localOk = Math.hypot(estimX-X_START, estimY-Y_START) <= 0.085
                && Math.abs(norm(yaw())) <= 4.0;
        } else {
            double a = Math.toRadians(yaw());
            double meiaY = (CHASSI/2.0) * (
                Math.abs(Math.sin(a)) + Math.abs(Math.cos(a)));
            double folgaSuperior = ARENA - (estimY + meiaY);
            localOk = Math.abs(estimX-X_LANCAMENTO_L2) <= 0.055
                && Math.abs(estimY-Y_L2_LANCAMENTO) <= 0.045
                && Math.abs(norm(180.0-yaw())) <= 2.5
                && folgaSuperior >= 0.008 && folgaSuperior <= 0.046;
        }
        if (!localOk) {
            recusasPose++;
            ultimoBloqueio = "Posicao invalida " + posto +
                " X=" + estimX + " Y=" + estimY + " yaw=" + yaw();
            return falha("INTERTRAVA POSE", ultimoBloqueio);
        }
        return true;
    }

    // ==========================================
    // CONTROLE DE RPM
    // ==========================================

    // Metodo REUTILIZA o historico de RPM ja obtido no deslocamento.
    // Exige nova estabilizacao apos cada alimentacao (reset no pulso).
    private boolean aguardarRpmEIntervalo(
        double primeiroPermitido,
        double prazo
    ) {
        if (!controleShooterAtivo) ativarControleShooter();
        estadoShooter = aguardandoRecuperacao
            ? "RECUPERANDO" : "AGUARDANDO_RPM";

        while (opModeIsActive() && !prazoEsgotado()
                && global.seconds() < prazo) {
            atualizarRPMContinuo();
            if (encoderShooterFalhou) {
                return falha("SHOOTER", "Encoder do MS nao gira");
            }
            double agora = global.seconds();
            // Sem amostras antigas: a ultima medicao precisa ser recente.
            boolean amostraRecente = rpmAmostraValida
                && agora - instanteUltimaAmostraValida <= 0.16;
            if (agora >= primeiroPermitido
                && amostraRecente
                && amostrasRpmEstaveis >= AMOSTRAS_RPM_ESTAVEL) {
                estadoShooter = "PRONTO";
                aguardandoRecuperacao = false;
                return true;
            }
            idle();
        }
        return false;
    }

    private void ativarControleShooter() {
        if (global == null || MS == null) return;
        if (MI != null && MI.getPower() > 0.50 && !alimentacaoAutorizada) {
            intertravamentos++;
            ultimoBloqueio = "Tentativa aquecer MS enquanto coleta";
            desativarControleShooter(MS_COLETA_SEGURO);
            estadoShooter = "BLOQUEADO_POR_COLETA";
            return;
        }
        controleShooterAtivo = true;
        encoderShooterFalhou = false;
        rpmAmostraValida = false;
        amostrasRpmEstaveis = 0;
        int ticks = MS.getCurrentPosition();
        double agora = global.seconds();
        encoderMSAnterior = ticks;
        encoderMSAoAtivar = ticks;
        encoderMSObservado = ticks;
        instanteMSAtivado = agora;
        instanteAmostraMS = agora;
        instanteUltimaMudancaMS = agora;
        instanteUltimaAmostraValida = -1;
        MS.setPower(MS_SHOT);
        potenciaMSComandada = MS_SHOT;
        estadoShooter = "SPINUP";
    }

    private void desativarControleShooter(double power) {
        controleShooterAtivo = false;
        if (MS != null) MS.setPower(power);
        potenciaMSComandada = power;
        amostrasRpmEstaveis = 0;
        rpmAmostraValida = false;
    }

    private void atualizarRPMContinuo() {
        // Seguranca redundante: se MI>0.50 e nao ha autorizacao de
        // disparo, derrubar MS ANTES da proxima leitura do simulador.
        if (!alimentacaoAutorizada && MI != null && MS != null
                && MI.getPower() > 0.50 && MS.getPower() > 0.50) {
            intertravamentos++;
            ultimoBloqueio = "MI/MS durante " + modoMecanismo;
            desativarControleShooter(MS_COLETA_SEGURO);
            estadoShooter = "BLOQUEADO_POR_COLETA";
            return;
        }
        if (!controleShooterAtivo || global == null || MS == null
                || encoderShooterFalhou) return;
        double agora = global.seconds();
        int encoderAtual = MS.getCurrentPosition();
        if (Math.abs(encoderAtual - encoderMSObservado) >= 2) {
            instanteUltimaMudancaMS = agora;
            encoderMSObservado = encoderAtual;
        }

        if (agora - instanteAmostraMS >= JANELA_RPM) {
            double dt = agora - instanteAmostraMS;
            rpmMS = Math.abs(encoderAtual - encoderMSAnterior)
                * 60.0 / (TICKS_POR_VOLTA_MS * dt);
            encoderMSAnterior = encoderAtual;
            instanteAmostraMS = agora;
            instanteUltimaAmostraValida = agora;
            rpmAmostraValida = true;
            potenciaMSComandada = Range.clip(
                MS_SHOT + KP_RPM_MS * (RPM_ALVO_MS_SIM - rpmMS),
                0.48, MS_POWER_MAX
            );
            MS.setPower(potenciaMSComandada);
            boolean dentroFaixa = rpmMS >= RPM_MIN_MS
                && rpmMS <= RPM_MAX_MS;
            amostrasRpmEstaveis = dentroFaixa
                ? Math.min(AMOSTRAS_RPM_ESTAVEL, amostrasRpmEstaveis + 1)
                : 0;
            if (aguardandoRecuperacao) {
                rpmMinRecuperacao = Double.isNaN(rpmMinRecuperacao)
                    ? rpmMS : Math.min(rpmMinRecuperacao, rpmMS);
            }
            if (!dentroFaixa)
                estadoShooter = aguardandoRecuperacao
                    ? "RECUPERANDO" : "AJUSTANDO_RPM";
        }
        // Watchdog de encoder. Apos ~0.6 s sem ticks, bloquear tiro.
        if (agora - instanteMSAtivado >= TIMEOUT_ENCODER_MS
            && agora - instanteUltimaMudancaMS >= TIMEOUT_ENCODER_MS) {
            encoderShooterFalhou = true;
            MS.setPower(0);
            potenciaMSComandada = 0;
            estadoShooter = "ERRO_ENCODER";
        }
    }

    // Estima a reserva pelo percurso reto medido, com limites conservadores.
    // Nao e uma previsao de tempo garantida: validar no Meca Sim.
    private double calcularReservaPark() {
        double v = Double.isNaN(velocidadeRetaMedida)
            ? VELOCIDADE_RETA_REFERENCIA
            : Range.clip(velocidadeRetaMedida, 0.50, 1.00);
        // Se o corredor foi lento, reter mais tempo; se foi rapido,
        // oferecer mais tempo ao terceiro lançamento.
        return Range.clip(2.20 + 0.85 * (0.82 - v),
            RESERVA_PARK_MIN, RESERVA_PARK_MAX);
    }

    private void marcarFase(int indice) {
        if (global == null || indice < 0 || indice >= temposFases.length)
            return;
        double agora = global.seconds();
        temposFases[indice] = Math.max(0, agora - inicioFase);
        inicioFase = agora;
    }

    private void medirVelocidadeLivre() {
        if (global == null) return;
        // No corredor: alinhado a 180 graus, longe das extremidades.
        if (Math.abs(norm(180.0 - yaw())) > 7.0 ||
            estimY < 0.85 || estimY > 2.60 ||
            Math.abs(estimX - X_LANCAMENTO_L2) > 0.25) return;
        if (Double.isNaN(referenciaVelX)) {
            referenciaVelX = estimX;
            referenciaVelY = estimY;
            referenciaVelT = global.seconds();
            return;
        }
        double dt = global.seconds() - referenciaVelT;
        if (dt >= 0.18) {
            double v = Math.hypot(estimX - referenciaVelX,
                estimY - referenciaVelY) / dt;
            if (v >= 0.15 && v <= 1.20)
                velocidadeRetaMedida = Double.isNaN(velocidadeRetaMedida)
                    ? v : 0.72 * velocidadeRetaMedida + 0.28 * v;
            referenciaVelX = estimX;
            referenciaVelY = estimY;
            referenciaVelT = global.seconds();
        }
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
            atualizarRPMContinuo();
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

            // v10: na RE do Garden nao existe parada no fim do segmento.
            // O robo entra imediatamente na curva longa de L2, ainda em re.
            // Na coleta de ida (mudanca de sentido) a parada e obrigatoria.
            if (restante <= (re ? 0.045 : 0.013)) {
                if (!re) parar();
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
            // v10: no retorno a L2, essa nao e mais uma meta de PARADA.
            // Freia somente ate velocidade de transferencia (~0.45 power).
            // A coleta de ida conserva o freio antigo junto a parede.
            double distanciaFreio = re ? Math.max(restante, 0.115) : restante;
            double freio = Math.sqrt(2.0 * 1.0 * Math.max(distanciaFreio, 0.0));
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
            atualizarRPMContinuo();
            idle();
        }

        return falha("GARDEN",
            prazoEsgotado() ? "Tempo global" : "Timeout coleta/retorno");
    }

    /**
     * Curva continua L2 -> Flower. Metodo ISOLADO: nao altera o
     * controlador das curvas que ja funcionaram (Garden-L2, retorno e PARK).
     *
     * O robo sai apontando -Y, curva pela area livre acima do HIVE,
     * e chega a (1.30, 3.06) apontando +Y.
     * Bezier com realimentacao por rumo do perfil + ponto adiante.
     */
    private boolean entrarFlowerEmCurva() {
        etapaAnterior = etapaAtual;
        etapaAtual = "L2 -> Flower em curva unica";
        proximaEtapa = "Coleta Flower superior";
        atualizarOdom();
        double iniX = estimX, iniY = estimY;
        final double p1x = 1.574, p1y = 2.750;
        final double p2x = 1.350, p2y = 2.950;
        final double fimX = X_FLOWER, fimY = Y_FLOWER_PRE;
        ElapsedTime timer = new ElapsedTime();
        double ultimoX = estimX, ultimoY = estimY;
        double ultimoProgresso = 0;
        int indice = 0;
        while (opModeIsActive() && !prazoEsgotado()
                && timer.seconds() < 3.8) {
            atualizarOdom();
            double restante = Math.hypot(fimX - estimX, fimY - estimY);
            double erroFim = Math.abs(norm(0.0 - yaw()));
            if (restante < 0.030 && erroFim < 2.5) {
                parar();
                return true;
            }
            double a = Math.toRadians(yaw());
            double m = CHASSI / 2.0 * (
                Math.abs(Math.sin(a)) + Math.abs(Math.cos(a)));
            if (Math.min(Math.min(estimX - m, ARENA - estimX - m),
                    Math.min(estimY - m, ARENA - estimY - m)) < 0.010) {
                return falha("FLOWER CURVA", "Folga do chassi insuficiente");
            }
            int melhor = indice;
            double menor = Double.POSITIVE_INFINITY;
            for (int i = Math.max(0, indice - 3); i <= 120; i++) {
                double t = i / 120.0;
                double bx = bezier(iniX,p1x,p2x,fimX,t);
                double by = bezier(iniY,p1y,p2y,fimY,t);
                double d2 = (bx-estimX)*(bx-estimX)
                    + (by-estimY)*(by-estimY);
                if (d2 < menor) { menor = d2; melhor = i; }
            }
            indice = Math.max(indice, melhor);
            double tVista = Math.min(1, (indice + 4) / 120.0);
            double alvoX = bezier(iniX,p1x,p2x,fimX,tVista);
            double alvoY = bezier(iniY,p1y,p2y,fimY,tVista);
            double dx = alvoX-estimX;
            double dy = alvoY-estimY;
            double rumoVista = norm(Math.toDegrees(Math.atan2(-dx,dy)));
            double rumoPerfil = norm(180.0 - 180.0*tVista);
            double erroPerfil = norm(rumoPerfil - yaw());
            double erroVista = norm(rumoVista - yaw());
            double correcao = Range.clip(
                0.026*Range.clip(erroPerfil,-25,25)
                + 0.010*Range.clip(erroVista,-25,25),
                -0.40,0.40);
            double base = Math.min(0.50, Math.min(
                0.28 + timer.seconds()*1.8,
                Math.max(0.13,Math.sqrt(2*1.05*restante))));
            if (Math.abs(erroPerfil)>12) base=Math.min(base,0.28);
            if (restante < 0.20) base=Math.min(base,0.22);
            if (restante < 0.07) base=Math.min(base,0.13);
            ME.setPower(Range.clip(base-correcao,-1,1));
            MD.setPower(Range.clip(base+correcao,-1,1));
            if (Math.hypot(estimX-ultimoX,estimY-ultimoY)>=0.012) {
                ultimoX=estimX; ultimoY=estimY;
                ultimoProgresso=timer.seconds();
            }
            if (timer.seconds()>0.85
                    && timer.seconds()-ultimoProgresso>0.75)
                return falha("FLOWER CURVA", "Sem progresso");
            atualizarRPMContinuo();
            idle();
        }
        return falha("FLOWER CURVA", "Timeout: chegada ou yaw fora da tolerancia");
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
    // CURVAS CONTINUAS COM ENCODERS + IMU
    // ==========================================
    // Bézier cubica: P0 atual, P1, P2, P3 destino.
    // Um unico loop controla giro e deslocamento; nenhum giro/parada
    // nos pontos de controle. O fim tem tolerancia posicional e angular.

    private static double bezier(double a, double b, double c,
                                 double d, double t) {
        double u = 1.0 - t;
        return u*u*u*a + 3*u*u*t*b + 3*u*t*t*c + t*t*t*d;
    }

    private boolean seguirBezier(
        String nome, String seguinte, boolean re,
        double p1x, double p1y,
        double p2x, double p2y,
        double fimX, double fimY,
        double potenciaMax, double yawFinal, double timeout,
        int antecipacao, double toleranciaFinal
    ) {
        etapaAnterior = etapaAtual;
        etapaAtual = nome;
        proximaEtapa = seguinte;
        atualizarOdom();

        final double inicioX = estimX;
        final double inicioY = estimY;

        ElapsedTime timer = new ElapsedTime();
        double ultimoX = estimX;
        double ultimoY = estimY;
        double tempoProgresso = 0.0;
        int indiceAnterior = 0;
        double ultimoLog = -1.0;
        final int N = 120;

        while (opModeIsActive() && !prazoEsgotado()
                && timer.seconds() < timeout) {

            atualizarOdom();
            medirVelocidadeLivre();
            double agora = timer.seconds();
            double distanciaFinal = Math.hypot(
                fimX - estimX, fimY - estimY);
            double erroFinalYaw = Math.abs(norm(yawFinal - yaw()));

            // O shooter pode permanecer ligado; neste metodo MI/SA
            // jamais sao acionados.
            if (distanciaFinal <= toleranciaFinal && erroFinalYaw <= 2.5) {
                parar();
                return true;
            }

            // Folgas dinamicas calculadas com a orientacao REAL do chassi.
            double a = Math.toRadians(yaw());
            double meia = CHASSI / 2.0 * (
                Math.abs(Math.cos(a)) + Math.abs(Math.sin(a)));
            double folgaXesq = estimX - meia;
            double folgaXdir = ARENA - (estimX + meia);
            double folgaYinf = estimY - meia;
            double folgaYsup = ARENA - (estimY + meia);

            if (folgaXesq < 0.008 || folgaXdir < 0.008 ||
                folgaYinf < 0.008 || folgaYsup < 0.008) {
                return falha("CURVA PAREDE",
                    nome + ": folgas E/D/I/S=" +
                    folgaXesq + "/" + folgaXdir + "/" +
                    folgaYinf + "/" + folgaYsup);
            }

            // Projecao da odometria na curva. Busca monotona evita
            // comandar movimento de volta por ruido no encoder.
            int melhor = indiceAnterior;
            double dist2Melhor = Double.POSITIVE_INFINITY;
            for (int i = Math.max(0, indiceAnterior - 3);
                 i <= N; i++) {
                double t = (double)i / N;
                double bx = bezier(inicioX,p1x,p2x,fimX,t);
                double by = bezier(inicioY,p1y,p2y,fimY,t);
                double d2 = (bx-estimX)*(bx-estimX) +
                            (by-estimY)*(by-estimY);
                if (d2 < dist2Melhor) {
                    dist2Melhor=d2;
                    melhor=i;
                }
            }
            indiceAnterior = Math.max(indiceAnterior, melhor);

            // Olha adiante na curva. A direcao do motor e invertida na re,
            // mas a orientacao da IMU e a mesma referencia de sempre.
            double tVista = Math.min(1.0,
                (double)Math.min(N, indiceAnterior + antecipacao) / N);
            double alvoX = bezier(inicioX,p1x,p2x,fimX,tVista);
            double alvoY = bezier(inicioY,p1y,p2y,fimY,tVista);
            double dx = alvoX - estimX;
            double dy = alvoY - estimY;
            double rumo = distanciaFinal < 0.15
                ? yawFinal
                : norm(Math.toDegrees(Math.atan2(
                    re ? dx : -dx, re ? -dy : dy)));
            double erro = norm(rumo - yaw());
            double correcao = Range.clip(0.020 *
                Range.clip(erro,-24.0,24.0),-0.32,0.32);

            // Aceleracao e frenagem suaves; desacelerar antes da parede.
            // v10: preservar a velocidade de saida do Garden somente
            // nesta curva. Outros trechos continuam com rampa de 0.29.
            double inicioPotencia = nome.equals("Garden -> L2 em curva continua")
                ? 0.45 : 0.29;
            double rampa = Range.clip(inicioPotencia + agora * 1.8,
                inicioPotencia, potenciaMax);
            double freio = Math.sqrt(
                2.0 * 1.05 * Math.max(0.0, distanciaFinal));
            double velocidade = Math.min(potenciaMax,
                Math.min(rampa, Math.max(0.13, freio)));

            // Quase na parede superior: freio ANTES do contato.
            if (fimY > 3.35 && estimY > 3.00)
                velocidade = Math.min(velocidade,0.60);
            if (fimY > 3.35 && estimY > 3.30)
                velocidade = Math.min(velocidade,0.35);
            if (fimY > 3.35 && estimY > 3.385)
                velocidade = Math.min(velocidade,0.20);
            // PARK a 3cm da parede esquerda: chegada em baixa potencia.
            if (fimX < 0.30 && estimX < 0.53)
                velocidade = Math.min(velocidade,0.34);
            if (fimX < 0.30 && estimX < 0.36)
                velocidade = Math.min(velocidade,0.20);
            // Angulo fora do previsto: nao ir rapido com grande erro.
            if (Math.abs(erro) > 16.0)
                velocidade = Math.min(velocidade,0.32);

            double base = (re ? -1.0 : 1.0) * velocidade;
            ME.setPower(Range.clip(base-correcao,-1.0,1.0));
            MD.setPower(Range.clip(base+correcao,-1.0,1.0));

            if (Math.hypot(estimX-ultimoX, estimY-ultimoY) >= 0.012) {
                ultimoX=estimX;
                ultimoY=estimY;
                tempoProgresso=agora;
            }
            if (agora>0.85 && agora-tempoProgresso>0.75)
                return falha("CURVA", nome+": sem progresso encoders");

            if (agora-ultimoLog > 0.18) {
                telemetry.addData("Curva",nome);
                telemetry.addData("T sim",global.seconds());
                telemetry.addData("X/Y","%.3f / %.3f",estimX,estimY);
                telemetry.addData("Yaw/Alvo","%.1f / %.1f",yaw(),rumo);
                telemetry.addData("Erro final",distanciaFinal);
                telemetry.addData("Folga sup/esq","%.3f / %.3f",
                    folgaYsup,folgaXesq);
                telemetry.update();
                ultimoLog=agora;
            }
            atualizarRPMContinuo();
            idle();
        }
        return falha("CURVA",prazoEsgotado()
            ? nome+": limite global"
            : nome+": timeout / alvo nao alcancado");
    }

    private boolean confirmarPoseL2(String fase) {
        atualizarOdom();
        double a = Math.toRadians(yaw());
        double meiaY = CHASSI/2.0 * (
            Math.abs(Math.sin(a))+Math.abs(Math.cos(a)));
        double folga = ARENA - (estimY + meiaY);
        double erroX = Math.abs(X_LANCAMENTO_L2 - estimX);
        double erroY = Math.abs(Y_L2_LANCAMENTO - estimY);
        double erroAng = Math.abs(norm(180.0 - yaw()));
        if (folga < 0.008 || folga > 0.046 ||
            erroX > 0.055 || erroY > 0.045 || erroAng > 2.5) {
            return falha("POSE L2", fase +
                ": X="+estimX+" Y="+estimY+
                " yaw="+yaw()+" folga="+folga);
        }
        telemetria(fase+": alinhado, liberar tiro so com RPM estavel");
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
            atualizarRPMContinuo();
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
                atualizarRPMContinuo();
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
            atualizarRPMContinuo();
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

                atualizarRPMContinuo();

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

            atualizarRPMContinuo();

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

            atualizarRPMContinuo();

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
        telemetry.addData("Ultimo feed", ultimoEventoFeed);
        telemetry.addData("Intervalo L2 real (s)", intervaloEfetivoL2);
        telemetry.addData("Intervalo 3o real (s)", intervaloEfetivoFlower);
        telemetry.addData("Pulsos nao sao bolas", "Conferir coletores / elementos no Meca Sim");
        telemetry.addData("3o L2 chegou", chegadaTerceiroL2);
        telemetry.addData("3o L2 prazo tiro", deadlineTerceiro);
        telemetry.addData("3o L2 primeiro/ultimo pulso",
            primeiroPulsoTerceiro + " / " + ultimoPulsoTerceiro);
        telemetry.addData("3o L2 diagnostico", diagnosticoTerceiro);
        telemetry.addData("Flower percorrida", flowerPercorrida);
        telemetry.addData("Mecanismo (v12)", modoMecanismo);
        telemetry.addData("Alimentacao autorizada", alimentacaoAutorizada);
        telemetry.addData("Intertravamentos MI/MS", intertravamentos);
        telemetry.addData("Recusas por pose", recusasPose);
        telemetry.addData("Ultimo bloqueio", ultimoBloqueio);
        telemetry.addData(
            "PARK alvo",
            X_PARK + ", " + Y_PARK
        );

        telemetry.addData("PARK reserva dinamica", reservaParkCalculada);
        telemetry.addData("Vel reta medida m/s", velocidadeRetaMedida);
        telemetry.addData("RPM monitor ativo", controleShooterAtivo);
        telemetry.addData("RPM amostra recente", rpmAmostraValida);
        for (int i = 0; i < temposFases.length; i++) {
            telemetry.addData("Tempo " + nomesFases[i], temposFases[i]);
        }
        telemetry.addData(
            "Estado",
            mensagem
        );

        telemetry.update();
    }
}
