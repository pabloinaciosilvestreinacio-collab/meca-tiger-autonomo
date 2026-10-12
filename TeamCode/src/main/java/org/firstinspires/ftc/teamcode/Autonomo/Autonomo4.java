package org.firstinspires.ftc.teamcode.Autonomo;

import android.util.Size;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@Autonomous(name = "Autonomo 4.0", group = "Teste")
public class Autonomo4 extends LinearOpMode {

    private DcMotor MD;
    private DcMotor ME;
    private DcMotor MI;
    private DcMotor MS;
    private IMU imu;

    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;

    private final ElapsedTime cronometroAuto = new ElapsedTime();

    private String etapaAtual = "Inicializacao";
    private String motivoFalha = "";
    private String estadoVisao = "Nao inicializada";

    private double rumoDoDeslocamento;
    private double rumoAntesDaVisao;

    private boolean visaoDisponivel;
    private boolean visaoUsada;
    private boolean alinhamentoVisualConcluido;

    private static final double ANGULO_COLETA = -50.0;
    private static final double DISTANCIA_COLETA_MM = 2000.0;
    private static final double VELOCIDADE_COLETA = 0.35;
    private static final double ANGULO_LANCAMENTO = 100.0;
    private static final int QUANTIDADE_LANCAMENTOS = 3;

    private static final double TICKS_POR_REV_MOTOR = 28.0;

    // UltraPlanetary REV: relacoes reais dos cartuchos, nao as nominais.
    private static final double REDUCAO_CARTUCHO_4_PARA_1 = 3.61;
    private static final double REDUCAO_CARTUCHO_5_PARA_1 = 5.23;
    private static final double REDUCAO_TOTAL =
            REDUCAO_CARTUCHO_4_PARA_1 * REDUCAO_CARTUCHO_5_PARA_1;

    private static final double DIAMETRO_RODA_MM = 90.0;

    // Ajustar somente depois de medir no robo real:
    // fator = distancia_comandada / distancia_real_medida.
    private static final double FATOR_CALIBRACAO_DISTANCIA = 1.0;

    private static final double TICKS_POR_MM =
            ((TICKS_POR_REV_MOTOR * REDUCAO_TOTAL)
                    / (Math.PI * DIAMETRO_RODA_MM))
                    * FATOR_CALIBRACAO_DISTANCIA;

    private static final double KP_VIRADA = 0.012;
    private static final double KI_VIRADA = 0.00012;
    private static final double KD_VIRADA = 0.0015;

    private static final double KP_RUMO = 0.018;
    private static final double KI_RUMO = 0.00015;
    private static final double KD_RUMO = 0.0012;

    private static final double LIMITE_INTEGRAL = 250.0;
    private static final double TEMPO_FILTRO_DERIVADA = 0.08;

    private static final double MIN_POTENCIA_VIRADA = 0.10;
    private static final double MAX_POTENCIA_VIRADA = 0.50;
    private static final double TOLERANCIA_VIRADA = 2.0;
    private static final double TOLERANCIA_VELOCIDADE_YAW = 8.0;
    private static final double TEMPO_ESTAVEL_VIRADA = 0.12;

    private static final double KP_ENCODER = 0.0002;
    private static final double MAX_CORRECAO_ENCODER = 0.05;
    private static final double MAX_CORRECAO_TOTAL = 0.25;

    private static final double TEMPO_ACELERACAO_DESLOCAMENTO = 0.40;
    private static final double FRACAO_DESACELERACAO = 0.20;
    private static final double MIN_POTENCIA_DESLOCAMENTO = 0.16;
    private static final double MIN_POTENCIA_RODA = 0.08;

    private static final int TOLERANCIA_DISTANCIA_TICKS = 25;
    private static final double TOLERANCIA_RUMO_DESLOCAMENTO = 5.0;
    private static final double TEMPO_CORRECAO_FINAL_RUMO = 1.50;

    private static final int PROGRESSO_MINIMO_TICKS = 6;
    private static final double TEMPO_SEM_PROGRESSO = 1.20;
    private static final int LIMITE_ENCODER_NEGATIVO = -10;

    private static final double POTENCIA_INTAKE = 0.60;
    private static final double POTENCIA_LANCAMENTO = 0.70;
    private static final double TEMPO_ACELERACAO_LANCADOR = 0.80;
    private static final double TEMPO_ALIMENTACAO_POR_PECA = 0.45;
    private static final double TEMPO_ENTRE_PECAS = 0.15;

    private static final String NOME_WEBCAM = "Webcam 1";

    // A Logitech C270 possui calibracao oficial do SDK em 640x480.
    private static final int CAMERA_LARGURA = 640;
    private static final int CAMERA_ALTURA = 480;
    private static final int DECIMACAO_APRILTAG = 2;

    // Corrige eventual desalinhamento mecanico entre o eixo optico
    // da camera e o eixo real de lancamento do robo.
    // Positivo desloca o alvo desejado para bearing positivo.
    private static final double OFFSET_BEARING_CAMERA_GRAUS = 0.0;

    // Clusters oficiais BIOBUZZ 2026-2027.
    private static final String CLUSTER_RED_SCORING = "RED SCORING";
    private static final String CLUSTER_RED_AUDIENCE = "RED AUDIENCE";
    private static final String CLUSTER_BLUE_SCORING = "BLUE SCORING";
    private static final String CLUSTER_BLUE_AUDIENCE = "BLUE AUDIENCE";

    private static final String[] CLUSTERS_BIOBUZZ = {
            CLUSTER_RED_SCORING,
            CLUSTER_RED_AUDIENCE,
            CLUSTER_BLUE_SCORING,
            CLUSTER_BLUE_AUDIENCE
    };

    // Estrategia do AUTO. Altere estas duas variaveis conforme o lado de partida.
    private static final Alianca ALIANCA_ESTRATEGIA = Alianca.RED;
    private static final LadoCampo LADO_ESTRATEGIA = LadoCampo.SCORING;

    private static final double KP_ALINHAMENTO_VISAO = 0.010;
    private static final double MIN_POTENCIA_ALINHAMENTO = 0.10;
    private static final double MAX_POTENCIA_ALINHAMENTO = 0.25;

    private static final double TOLERANCIA_BEARING_GRAUS = 2.0;
    private static final double TEMPO_CONFIRMACAO_DETECCAO = 0.18;
    private static final double TEMPO_ESTAVEL_ALINHAMENTO = 0.20;
    private static final double TEMPO_PERDA_ALVO = 0.35;
    private static final double TEMPO_LIMITE_VISAO = 2.50;
    private static final double TEMPO_LIMITE_RETORNO_RUMO = 1.60;
    private static final double MARGEM_DECISAO_VISAO = 0.35;

    private static final double TEMPO_LIMITE_VIRADA = 7.0;
    private static final double TEMPO_LIMITE_DISTANCIA = 12.0;
    private static final double TEMPO_MAXIMO_AUTO = 30.0;
    private static final double RESERVA_SEGURANCA = 1.0;

    @Override
    public void runOpMode() {
        try {
            configurarHardware();
            boolean imuInicializado = configurarImu();
            boolean configuracaoValida = validarConfiguracao();

            if (configuracaoValida) {
                configurarVisao();
            } else {
                estadoVisao = "Nao iniciada: configuracao invalida";
            }

            reiniciarEncoders();

            telemetry.addLine("Meca Tiger - Autonomo 4.0");
            telemetry.addData("IMU inicializado", imuInicializado);
            telemetry.addData("Visao", estadoVisao);
            telemetry.addData("Cluster alvo", clusterAlvoDaEstrategia());
            telemetry.addData("Ticks/mm", "%.4f", TICKS_POR_MM);
            telemetry.addLine("-50, coletar 2000 mm, +100, alinhar por visao, lancar 3");
            telemetry.update();

            waitForStart();

            if (isStopRequested()) {
                return;
            }

            if (!configuracaoValida) {
                mostrarFalha();
                return;
            }

            if (!imuInicializado) {
                motivoFalha = "IMU nao inicializado";
                mostrarFalha();
                return;
            }

            imu.resetYaw();
            cronometroAuto.reset();

            boolean sucesso = sequencia(
                    "Autonomo 4.0",
                    tarefa("Virar para coleta",
                            () -> virarRelativo(ANGULO_COLETA)),
                    tarefa("Capturar rumo do deslocamento",
                            this::capturarRumoDeslocamento),
                    tarefa("Coletar andando",
                            () -> coletarDuranteDeslocamento(
                                    DISTANCIA_COLETA_MM,
                                    rumoDoDeslocamento,
                                    VELOCIDADE_COLETA)),
                    tarefa("Virar para lancamento",
                            () -> virarRelativo(ANGULO_LANCAMENTO)),
                    tarefa("Capturar rumo antes da visao",
                            this::capturarRumoAntesDaVisao),
                    tarefa("Alinhar por visao ou manter IMU",
                            this::alinharComVisaoOuFallback),
                    tarefa("Lancar pecas",
                            () -> lancarPecasPorTempo(
                                    QUANTIDADE_LANCAMENTOS))
            );

            if (!sucesso) {
                mostrarFalha();
                return;
            }

            pararTudo();

            telemetry.addLine("Autonomo 4.0 concluido");
            telemetry.addData("Ultima etapa", etapaAtual);
            telemetry.addData("Tempo total", "%.2f s", cronometroAuto.seconds());
            telemetry.addData("Yaw final", "%.2f", lerYaw());
            telemetry.addData("Visao usada", visaoUsada);
            telemetry.addData("Alinhamento visual", alinhamentoVisualConcluido);
            telemetry.addData("Estado da visao", estadoVisao);
            telemetry.addData("Encoder MD", MD.getCurrentPosition());
            telemetry.addData("Encoder ME", ME.getCurrentPosition());
            telemetry.update();

        } catch (RuntimeException e) {
            etapaAtual = "Excecao inesperada";
            motivoFalha = "Falha de runtime: "
                    + e.getClass().getSimpleName();
            mostrarFalha();
        } finally {
            pararTudo();
            fecharVisao();
        }
    }

    private boolean validarConfiguracao() {
        etapaAtual = "Validacao de configuracao";

        if (!Double.isFinite(TICKS_POR_MM) || TICKS_POR_MM <= 0.0) {
            motivoFalha = "TICKS_POR_MM invalido";
            return false;
        }

        if (TICKS_POR_REV_MOTOR <= 0.0
                || REDUCAO_CARTUCHO_4_PARA_1 <= 0.0
                || REDUCAO_CARTUCHO_5_PARA_1 <= 0.0
                || DIAMETRO_RODA_MM <= 0.0
                || FATOR_CALIBRACAO_DISTANCIA <= 0.0) {
            motivoFalha = "Parametros mecanicos invalidos";
            return false;
        }

        if (Math.abs(ANGULO_COLETA) > 180.0
                || Math.abs(ANGULO_LANCAMENTO) > 180.0
                || DISTANCIA_COLETA_MM <= 0.0
                || VELOCIDADE_COLETA < MIN_POTENCIA_DESLOCAMENTO
                || VELOCIDADE_COLETA > 1.0) {
            motivoFalha = "Parametros da estrategia invalidos";
            return false;
        }

        if (MIN_POTENCIA_VIRADA <= 0.0
                || MIN_POTENCIA_VIRADA > MAX_POTENCIA_VIRADA
                || MIN_POTENCIA_ALINHAMENTO <= 0.0
                || MIN_POTENCIA_ALINHAMENTO > MAX_POTENCIA_ALINHAMENTO
                || MAX_CORRECAO_ENCODER > MAX_CORRECAO_TOTAL) {
            motivoFalha = "Limites de controle invalidos";
            return false;
        }

        if (CAMERA_LARGURA <= 0
                || CAMERA_ALTURA <= 0
                || DECIMACAO_APRILTAG <= 0) {
            motivoFalha = "Parametros da camera invalidos";
            return false;
        }

        if (!ehClusterBiobuzz(clusterAlvoDaEstrategia())) {
            motivoFalha = "Cluster da estrategia invalido";
            return false;
        }

        double tempoLancamento =
                calcularTempoLancamento(QUANTIDADE_LANCAMENTOS);

        if (QUANTIDADE_LANCAMENTOS <= 0
                || tempoLancamento + RESERVA_SEGURANCA
                >= TEMPO_MAXIMO_AUTO) {
            motivoFalha = "Orcamento de tempo do lancamento invalido";
            return false;
        }

        motivoFalha = "";
        etapaAtual = "Inicializacao";
        return true;
    }

    private void configurarHardware() {
        MD = hardwareMap.get(DcMotor.class, "MD");
        ME = hardwareMap.get(DcMotor.class, "ME");
        MI = hardwareMap.get(DcMotor.class, "MI");
        MS = hardwareMap.get(DcMotor.class, "MS");
        imu = obterImu();

        MD.setDirection(DcMotor.Direction.REVERSE);
        ME.setDirection(DcMotor.Direction.FORWARD);

        MD.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        ME.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        MI.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        MS.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        pararTudo();
    }

    private IMU obterImu() {
        try {
            return hardwareMap.get(IMU.class, "imu");
        } catch (RuntimeException e) {
            return hardwareMap.get(IMU.class, "IMU");
        }
    }

    private boolean configurarImu() {
        try {
            RevHubOrientationOnRobot orientacao =
                    new RevHubOrientationOnRobot(
                            RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                            RevHubOrientationOnRobot.UsbFacingDirection.UP
                    );

            return imu.initialize(new IMU.Parameters(orientacao));

        } catch (RuntimeException e) {
            motivoFalha = "Falha ao inicializar IMU: "
                    + e.getClass().getSimpleName();
            return false;
        }
    }

    private void configurarVisao() {
        visaoDisponivel = false;
        visaoUsada = false;
        alinhamentoVisualConcluido = false;

        try {
            aprilTag = new AprilTagProcessor.Builder().build();
            aprilTag.setDecimation(DECIMACAO_APRILTAG);

            visionPortal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, NOME_WEBCAM))
                    .setCameraResolution(
                            new Size(CAMERA_LARGURA, CAMERA_ALTURA))
                    .addProcessor(aprilTag)
                    .build();

            visaoDisponivel = true;
            estadoVisao = "Inicializada";

        } catch (RuntimeException e) {
            visaoDisponivel = false;
            estadoVisao = "Indisponivel: " + e.getClass().getSimpleName();
            fecharVisao();
        }
    }

    private boolean cameraEstaStreaming() {
        if (visionPortal == null) {
            return false;
        }

        try {
            return visionPortal.getCameraState()
                    == VisionPortal.CameraState.STREAMING;
        } catch (RuntimeException e) {
            estadoVisao = "Erro consultando camera: "
                    + e.getClass().getSimpleName();
            visaoDisponivel = false;
            return false;
        }
    }

    private boolean sequencia(String nome, Tarefa... tarefas) {
        telemetry.addData("Sequencia", nome);
        telemetry.update();

        for (Tarefa tarefa : tarefas) {
            if (!tempoDisponivel()) {
                motivoFalha = "Tempo AUTO esgotado ou STOP";
                return false;
            }

            motivoFalha = "";
            etapaAtual = tarefa.nome;

            telemetry.addData("Etapa", etapaAtual);
            telemetry.addData("Tempo AUTO", "%.2f s", cronometroAuto.seconds());
            telemetry.addData("Visao", estadoVisao);
            telemetry.update();

            if (!tarefa.acao.executar()) {
                if (motivoFalha.isEmpty()) {
                    motivoFalha = "Etapa retornou falha sem diagnostico";
                }
                return false;
            }
        }

        return true;
    }

    private Tarefa tarefa(String nome, Acao acao) {
        return new Tarefa(nome, acao);
    }

    private boolean capturarRumoDeslocamento() {
        rumoDoDeslocamento = lerYaw();

        if (!Double.isFinite(rumoDoDeslocamento)) {
            motivoFalha = "Leitura IMU invalida";
            return false;
        }

        return true;
    }

    private boolean capturarRumoAntesDaVisao() {
        rumoAntesDaVisao = lerYaw();

        if (!Double.isFinite(rumoAntesDaVisao)) {
            motivoFalha = "Leitura IMU invalida antes da visao";
            return false;
        }

        return true;
    }

    private boolean virarRelativo(double graus) {
        if (!Double.isFinite(graus) || Math.abs(graus) > 180.0) {
            motivoFalha = "Angulo relativo invalido";
            pararTracao();
            return false;
        }

        double rumoInicial = lerYaw();

        if (!Double.isFinite(rumoInicial)) {
            motivoFalha = "IMU invalido no inicio da virada";
            pararTracao();
            return false;
        }

        return virarParaRumo(
                normalizarAngulo(rumoInicial + graus),
                TEMPO_LIMITE_VIRADA
        );
    }

    private boolean virarParaRumo(double rumoAlvo, double limiteTempo) {
        if (!Double.isFinite(rumoAlvo)
                || !Double.isFinite(limiteTempo)
                || limiteTempo <= 0.0) {
            motivoFalha = "Parametros invalidos para virar por rumo";
            pararTracao();
            return false;
        }

        double tempoEstavelDesde = -1.0;

        ControladorPID pid = new ControladorPID(
                KP_VIRADA,
                KI_VIRADA,
                KD_VIRADA,
                LIMITE_INTEGRAL,
                TEMPO_FILTRO_DERIVADA
        );

        ElapsedTime timer = new ElapsedTime();
        ElapsedTime amostra = new ElapsedTime();
        ElapsedTime telemetriaTimer = new ElapsedTime();

        while (opModeIsActive()
                && tempoDisponivel()
                && timer.seconds() < limiteTempo) {

            double rumoAtual = lerYaw();

            if (!Double.isFinite(rumoAtual)) {
                motivoFalha = "IMU invalido durante virada";
                pararTracao();
                return false;
            }

            double erro = normalizarAngulo(rumoAlvo - rumoAtual);
            double dt = limitar(amostra.seconds(), 0.001, 0.10);
            amostra.reset();

            double comando = pid.calcular(
                    erro,
                    dt,
                    -MAX_POTENCIA_VIRADA,
                    MAX_POTENCIA_VIRADA
            );

            double velocidadeYaw = pid.getVelocidadeYaw();

            if (Math.abs(erro) <= TOLERANCIA_VIRADA
                    && Math.abs(velocidadeYaw)
                    <= TOLERANCIA_VELOCIDADE_YAW) {

                if (tempoEstavelDesde < 0.0) {
                    tempoEstavelDesde = timer.seconds();
                }

                if (timer.seconds() - tempoEstavelDesde
                        >= TEMPO_ESTAVEL_VIRADA) {
                    pararTracao();
                    return true;
                }

            } else {
                tempoEstavelDesde = -1.0;
            }

            if (Math.abs(erro) > TOLERANCIA_VIRADA
                    && Math.abs(comando) < MIN_POTENCIA_VIRADA) {
                comando = Math.copySign(MIN_POTENCIA_VIRADA, erro);
            }

            MD.setPower(comando);
            ME.setPower(-comando);

            if (telemetriaTimer.seconds() >= 0.10) {
                telemetry.addData("Etapa", etapaAtual);
                telemetry.addData("Yaw atual / alvo", "%.2f / %.2f",
                        rumoAtual, rumoAlvo);
                telemetry.addData("Erro / yaw graus/s", "%.2f / %.2f",
                        erro, velocidadeYaw);
                telemetry.addData("Potencia virada", "%.2f", comando);
                telemetry.update();
                telemetriaTimer.reset();
            }

            sleep(10);
        }

        pararTracao();
        motivoFalha = tempoDisponivel()
                ? "Tempo limite da virada"
                : "Tempo AUTO esgotado ou STOP";
        return false;
    }

    private boolean andarComRumo(
            double distanciaMm,
            double rumoAlvo,
            double velocidade) {

        if (!Double.isFinite(distanciaMm)
                || !Double.isFinite(rumoAlvo)
                || !Double.isFinite(velocidade)
                || distanciaMm <= 0.0
                || velocidade <= 0.0
                || velocidade > 1.0
                || distanciaMm * TICKS_POR_MM > Integer.MAX_VALUE) {
            motivoFalha = "Parametros de deslocamento invalidos";
            pararTracao();
            return false;
        }

        int alvoTicks = Math.max(
                1,
                (int) Math.round(distanciaMm * TICKS_POR_MM)
        );

        reiniciarEncoders();

        ControladorPID pidRumo = new ControladorPID(
                KP_RUMO,
                KI_RUMO,
                KD_RUMO,
                LIMITE_INTEGRAL,
                TEMPO_FILTRO_DERIVADA
        );

        ElapsedTime timer = new ElapsedTime();
        ElapsedTime amostra = new ElapsedTime();
        ElapsedTime semProgressoMD = new ElapsedTime();
        ElapsedTime semProgressoME = new ElapsedTime();
        ElapsedTime telemetriaTimer = new ElapsedTime();

        int ultimoProgressoMD = 0;
        int ultimoProgressoME = 0;

        while (opModeIsActive()
                && tempoDisponivel()
                && timer.seconds() < TEMPO_LIMITE_DISTANCIA) {

            int posicaoMD = MD.getCurrentPosition();
            int posicaoME = ME.getCurrentPosition();

            if (posicaoMD < LIMITE_ENCODER_NEGATIVO
                    || posicaoME < LIMITE_ENCODER_NEGATIVO) {
                motivoFalha = "Encoder negativo: verificar direcoes";
                pararTracao();
                return false;
            }

            int ticksMD = Math.max(0, posicaoMD);
            int ticksME = Math.max(0, posicaoME);
            double mediaTicks = ((double) ticksMD + ticksME) / 2.0;
            int diferencaTicks = ticksMD - ticksME;
            double restantes = Math.max(0.0, alvoTicks - mediaTicks);

            double rumoAtual = lerYaw();

            if (!Double.isFinite(rumoAtual)) {
                motivoFalha = "IMU invalido durante deslocamento";
                pararTracao();
                return false;
            }

            double erroRumo = normalizarAngulo(rumoAlvo - rumoAtual);

            if (mediaTicks >= alvoTicks - TOLERANCIA_DISTANCIA_TICKS) {
                pararTracao();

                if (Math.abs(erroRumo)
                        <= TOLERANCIA_RUMO_DESLOCAMENTO) {
                    return true;
                }

                // A distancia do centro do robo e dada pela media das rodas.
                // Depois de atingir o alvo linear, nao continue avancando apenas
                // para igualar encoders: corrija o rumo girando no proprio eixo.
                boolean rumoCorrigido = virarParaRumo(
                        rumoAlvo,
                        TEMPO_CORRECAO_FINAL_RUMO
                );

                if (!rumoCorrigido
                        && "Tempo limite da virada".equals(motivoFalha)) {
                    motivoFalha =
                            "Distancia atingida; correcao final de rumo excedeu o tempo";
                }

                return rumoCorrigido;
            }

            if (ticksMD >= ultimoProgressoMD + PROGRESSO_MINIMO_TICKS) {
                ultimoProgressoMD = ticksMD;
                semProgressoMD.reset();
            } else if (semProgressoMD.seconds() > TEMPO_SEM_PROGRESSO) {
                motivoFalha = "MD sem progresso nos encoders";
                pararTracao();
                return false;
            }

            if (ticksME >= ultimoProgressoME + PROGRESSO_MINIMO_TICKS) {
                ultimoProgressoME = ticksME;
                semProgressoME.reset();
            } else if (semProgressoME.seconds() > TEMPO_SEM_PROGRESSO) {
                motivoFalha = "ME sem progresso nos encoders";
                pararTracao();
                return false;
            }

            double dt = limitar(amostra.seconds(), 0.001, 0.10);
            amostra.reset();

            double correcaoRumo = pidRumo.calcular(
                    erroRumo,
                    dt,
                    -MAX_CORRECAO_TOTAL,
                    MAX_CORRECAO_TOTAL
            );

            double correcaoEncoder = limitar(
                    KP_ENCODER * diferencaTicks,
                    -MAX_CORRECAO_ENCODER,
                    MAX_CORRECAO_ENCODER
            );

            double fatorAceleracao = limitar(
                    timer.seconds() / TEMPO_ACELERACAO_DESLOCAMENTO,
                    0.0,
                    1.0
            );

            double faixaDesaceleracao = Math.max(
                    1.0,
                    alvoTicks * FRACAO_DESACELERACAO
            );

            double fatorDesaceleracao = limitar(
                    restantes / faixaDesaceleracao,
                    0.0,
                    1.0
            );

            double potenciaBase = Math.max(
                    MIN_POTENCIA_DESLOCAMENTO,
                    velocidade * Math.min(
                            fatorAceleracao,
                            fatorDesaceleracao)
            );

            double limiteCorrecao = Math.min(
                    MAX_CORRECAO_TOTAL,
                    Math.max(0.0, potenciaBase - MIN_POTENCIA_RODA)
            );

            double correcao = limitar(
                    correcaoRumo - correcaoEncoder,
                    -limiteCorrecao,
                    limiteCorrecao
            );

            double potenciaMD = limitar(
                    potenciaBase + correcao,
                    0.0,
                    1.0
            );

            double potenciaME = limitar(
                    potenciaBase - correcao,
                    0.0,
                    1.0
            );

            MD.setPower(potenciaMD);
            ME.setPower(potenciaME);

            if (telemetriaTimer.seconds() >= 0.10) {
                telemetry.addData("Etapa", etapaAtual);
                telemetry.addData("Ticks MD / ME", "%d / %d", ticksMD, ticksME);
                telemetry.addData("Alvo / restante", "%d / %.1f mm",
                        alvoTicks, restantes / TICKS_POR_MM);
                telemetry.addData("Erro rumo", "%.2f", erroRumo);
                telemetry.addData("Diferenca ticks", "%d", diferencaTicks);
                telemetry.addData("Correcao", "%.3f", correcao);
                telemetry.addData("Potencia MD / ME", "%.2f / %.2f",
                        potenciaMD, potenciaME);
                telemetry.addData("Tempo", "%.2f s", timer.seconds());
                telemetry.update();
                telemetriaTimer.reset();
            }

            sleep(10);
        }

        pararTracao();
        motivoFalha = tempoDisponivel()
                ? "Tempo limite do deslocamento"
                : "Tempo AUTO esgotado ou STOP";
        return false;
    }

    private boolean coletarDuranteDeslocamento(
            double distanciaMm,
            double rumoAlvo,
            double velocidade) {

        if (!tempoDisponivel()) {
            motivoFalha = "Tempo AUTO esgotado ou STOP";
            return false;
        }

        MI.setPower(POTENCIA_INTAKE);

        try {
            return andarComRumo(distanciaMm, rumoAlvo, velocidade);
        } finally {
            MI.setPower(0);
        }
    }

    private boolean alinharComVisaoOuFallback() {
        visaoUsada = false;
        alinhamentoVisualConcluido = false;

        if (!visaoDisponivel
                || aprilTag == null
                || visionPortal == null) {
            estadoVisao = "Fallback IMU: visao indisponivel";
            return true;
        }

        if (!cameraEstaStreaming()) {
            estadoVisao = "Fallback IMU: camera nao esta STREAMING";
            return true;
        }

        double tempoLancamento = calcularTempoLancamento(
                QUANTIDADE_LANCAMENTOS
        );

        double tempoMinimoNecessario =
                TEMPO_LIMITE_VISAO
                        + TEMPO_LIMITE_RETORNO_RUMO
                        + tempoLancamento
                        + MARGEM_DECISAO_VISAO;

        if (tempoRestanteSeguro() < tempoMinimoNecessario) {
            estadoVisao = "Fallback IMU: pouco tempo para usar visao";
            return true;
        }

        ElapsedTime timer = new ElapsedTime();
        ElapsedTime telemetriaTimer = new ElapsedTime();

        String alvoConfirmando = null;
        double inicioConfirmacao = -1.0;
        double inicioEstavel = -1.0;
        double ultimaDeteccao = -1.0;

        boolean movimentoIniciado = false;

        while (opModeIsActive()
                && tempoDisponivel()
                && timer.seconds() < TEMPO_LIMITE_VISAO) {

            AlvoVisao alvo = lerMelhorAlvo();

            if (alvo == null) {
                if (!movimentoIniciado) {
                    alvoConfirmando = null;
                    inicioConfirmacao = -1.0;
                    pararTracao();
                } else if (ultimaDeteccao >= 0.0
                        && timer.seconds() - ultimaDeteccao
                        > TEMPO_PERDA_ALVO) {

                    pararTracao();
                    estadoVisao = "Alvo perdido; retornando ao rumo do IMU";

                    if (!virarParaRumo(
                            rumoAntesDaVisao,
                            TEMPO_LIMITE_RETORNO_RUMO)) {
                        motivoFalha =
                                "Visao perdeu alvo e retorno por IMU falhou";
                        return false;
                    }

                    estadoVisao = "Fallback IMU apos perda do alvo";
                    return true;
                }

                if (telemetriaTimer.seconds() >= 0.10) {
                    telemetry.addData("Visao", "Sem alvo valido");
                    telemetry.addData("Tempo visao", "%.2f s", timer.seconds());
                    telemetry.update();
                    telemetriaTimer.reset();
                }

                sleep(10);
                continue;
            }

            ultimaDeteccao = timer.seconds();

            if (!alvo.chave.equals(alvoConfirmando)) {
                alvoConfirmando = alvo.chave;
                inicioConfirmacao = timer.seconds();
                inicioEstavel = -1.0;
                pararTracao();
            }

            if (timer.seconds() - inicioConfirmacao
                    < TEMPO_CONFIRMACAO_DETECCAO) {
                if (telemetriaTimer.seconds() >= 0.10) {
                    telemetry.addData("Visao", "Confirmando %s", alvo.nome);
                    telemetry.addData("Bearing", "%.2f deg", alvo.bearing);
                    telemetry.update();
                    telemetriaTimer.reset();
                }
                sleep(10);
                continue;
            }

            visaoUsada = true;
            movimentoIniciado = true;

            double erroBearing = normalizarAngulo(
                    alvo.bearing - OFFSET_BEARING_CAMERA_GRAUS
            );

            if (Math.abs(erroBearing) <= TOLERANCIA_BEARING_GRAUS) {
                pararTracao();

                if (inicioEstavel < 0.0) {
                    inicioEstavel = timer.seconds();
                }

                if (timer.seconds() - inicioEstavel
                        >= TEMPO_ESTAVEL_ALINHAMENTO) {
                    alinhamentoVisualConcluido = true;
                    estadoVisao = "Alinhado com " + alvo.nome;
                    return true;
                }

            } else {
                inicioEstavel = -1.0;

                double comando = limitar(
                        KP_ALINHAMENTO_VISAO * erroBearing,
                        -MAX_POTENCIA_ALINHAMENTO,
                        MAX_POTENCIA_ALINHAMENTO
                );

                if (Math.abs(comando) < MIN_POTENCIA_ALINHAMENTO) {
                    comando = Math.copySign(
                            MIN_POTENCIA_ALINHAMENTO,
                            erroBearing
                    );
                }

                MD.setPower(comando);
                ME.setPower(-comando);
            }

            if (telemetriaTimer.seconds() >= 0.10) {
                telemetry.addData("Visao", alvo.tipo);
                telemetry.addData("Alvo", alvo.nome);
                telemetry.addData("Range", "%.1f in", alvo.range);
                telemetry.addData("Bearing bruto", "%.2f deg", alvo.bearing);
                telemetry.addData("Erro bearing", "%.2f deg", erroBearing);
                telemetry.addData("Yaw alvo", "%.2f deg", alvo.yaw);
                telemetry.update();
                telemetriaTimer.reset();
            }

            sleep(10);
        }

        pararTracao();

        if (!movimentoIniciado) {
            estadoVisao = "Fallback IMU: nenhum alvo confirmado";
            return true;
        }

        estadoVisao = "Visao expirou; retornando ao rumo do IMU";

        if (!virarParaRumo(
                rumoAntesDaVisao,
                TEMPO_LIMITE_RETORNO_RUMO)) {
            motivoFalha = "Timeout da visao e retorno por IMU falhou";
            return false;
        }

        estadoVisao = "Fallback IMU apos timeout da visao";
        return true;
    }

    private AlvoVisao lerMelhorAlvo() {
        if (aprilTag == null) {
            return null;
        }

        List<AprilTagDetection> deteccoes;

        try {
            deteccoes = aprilTag.getDetections();
        } catch (RuntimeException e) {
            estadoVisao = "Erro lendo AprilTag: "
                    + e.getClass().getSimpleName();
            return null;
        }

        if (deteccoes == null || deteccoes.isEmpty()) {
            return null;
        }

        String clusterDesejado = clusterAlvoDaEstrategia();
        AlvoVisao melhorCluster = null;

        for (AprilTagDetection detection : deteccoes) {
            if (!(detection instanceof AprilTagClusterDetection)) {
                continue;
            }

            AprilTagClusterDetection cluster =
                    (AprilTagClusterDetection) detection;

            if (cluster.metadata == null || cluster.ftcPose == null) {
                continue;
            }

            String nome = cluster.metadata.shortName;

            if (nome == null || nome.isEmpty()) {
                nome = cluster.metadata.name;
            }

            if (!ehClusterBiobuzz(nome)
                    || !clusterDesejado.equalsIgnoreCase(nome)) {
                continue;
            }

            // BIOBUZZ: CELL pontuavel com camera voltada no sentido
            // do lancamento deve apresentar |roll| < 90 graus.
            if (!Double.isFinite(cluster.ftcPose.roll)
                    || Math.abs(cluster.ftcPose.roll) >= 90.0) {
                continue;
            }

            AlvoVisao candidato = new AlvoVisao(
                    "cluster:" + nome,
                    nome,
                    "Cluster",
                    cluster.ftcPose.range,
                    cluster.ftcPose.bearing,
                    cluster.ftcPose.yaw,
                    cluster.ftcPose.roll
            );

            if (dadosVisaoValidos(candidato)
                    && (melhorCluster == null
                    || Math.abs(candidato.bearing)
                    < Math.abs(melhorCluster.bearing))) {
                melhorCluster = candidato;
            }
        }

        return melhorCluster;
    }

    private String clusterAlvoDaEstrategia() {
        if (ALIANCA_ESTRATEGIA == Alianca.RED) {
            return LADO_ESTRATEGIA == LadoCampo.SCORING
                    ? CLUSTER_RED_SCORING
                    : CLUSTER_RED_AUDIENCE;
        }

        return LADO_ESTRATEGIA == LadoCampo.SCORING
                ? CLUSTER_BLUE_SCORING
                : CLUSTER_BLUE_AUDIENCE;
    }

    private boolean ehClusterBiobuzz(String nome) {
        if (nome == null) {
            return false;
        }

        for (String cluster : CLUSTERS_BIOBUZZ) {
            if (cluster.equalsIgnoreCase(nome)) {
                return true;
            }
        }

        return false;
    }

    private boolean dadosVisaoValidos(AlvoVisao alvo) {
        return alvo != null
                && Double.isFinite(alvo.range)
                && Double.isFinite(alvo.bearing)
                && Double.isFinite(alvo.yaw)
                && Double.isFinite(alvo.roll)
                && alvo.range > 0.0
                && Math.abs(alvo.bearing) <= 180.0
                && Math.abs(alvo.yaw) <= 180.0
                && Math.abs(alvo.roll) <= 180.0;
    }

    private boolean lancarPecasPorTempo(int quantidade) {
        if (quantidade <= 0) {
            motivoFalha = "Quantidade invalida";
            return false;
        }

        double tempoNecessario = calcularTempoLancamento(quantidade);

        if (!tempoDisponivel()
                || tempoRestanteSeguro() <= tempoNecessario) {
            motivoFalha = "Sem tempo para o lancamento completo";
            return false;
        }

        try {
            MS.setPower(POTENCIA_LANCAMENTO);

            if (!esperarAtivo(TEMPO_ACELERACAO_LANCADOR)) {
                motivoFalha = "Lancador interrompido ao acelerar";
                return false;
            }

            for (int i = 0; i < quantidade; i++) {
                MI.setPower(POTENCIA_INTAKE);

                if (!esperarAtivo(TEMPO_ALIMENTACAO_POR_PECA)) {
                    motivoFalha = "Alimentacao do lancador interrompida";
                    return false;
                }

                MI.setPower(0);

                telemetry.addData("Lancamento", "%d / %d", i + 1, quantidade);
                telemetry.update();

                if (i < quantidade - 1
                        && !esperarAtivo(TEMPO_ENTRE_PECAS)) {
                    motivoFalha = "Intervalo de lancamento interrompido";
                    return false;
                }
            }

            return true;

        } finally {
            pararMecanismos();
        }
    }

    private double calcularTempoLancamento(int quantidade) {
        if (quantidade <= 0) {
            return 0.0;
        }

        return TEMPO_ACELERACAO_LANCADOR
                + quantidade * TEMPO_ALIMENTACAO_POR_PECA
                + (quantidade - 1) * TEMPO_ENTRE_PECAS;
    }

    private boolean esperarAtivo(double segundos) {
        if (!Double.isFinite(segundos) || segundos < 0.0) {
            return false;
        }

        ElapsedTime timer = new ElapsedTime();

        while (opModeIsActive()
                && tempoDisponivel()
                && timer.seconds() < segundos) {
            sleep(10);
        }

        return opModeIsActive()
                && tempoDisponivel()
                && timer.seconds() >= segundos;
    }

    private void reiniciarEncoders() {
        pararTracao();

        MD.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ME.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        MD.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        ME.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    private double lerYaw() {
        try {
            return imu.getRobotYawPitchRollAngles()
                    .getYaw(AngleUnit.DEGREES);
        } catch (RuntimeException e) {
            return Double.NaN;
        }
    }

    private boolean tempoDisponivel() {
        return opModeIsActive()
                && cronometroAuto.seconds()
                < TEMPO_MAXIMO_AUTO - RESERVA_SEGURANCA;
    }

    private double tempoRestanteSeguro() {
        return Math.max(
                0.0,
                TEMPO_MAXIMO_AUTO
                        - RESERVA_SEGURANCA
                        - cronometroAuto.seconds()
        );
    }

    private double normalizarAngulo(double graus) {
        if (!Double.isFinite(graus)) {
            return Double.NaN;
        }

        double normalizado = graus % 360.0;

        if (normalizado > 180.0) {
            normalizado -= 360.0;
        } else if (normalizado < -180.0) {
            normalizado += 360.0;
        }

        return normalizado;
    }

    private double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private void mostrarFalha() {
        pararTudo();

        telemetry.addData("Autonomo interrompido", etapaAtual);
        telemetry.addData("Motivo", motivoFalha);
        telemetry.addData("Tempo AUTO", "%.2f s", cronometroAuto.seconds());
        telemetry.addData("Yaw", "%.2f", lerYaw());
        telemetry.addData("Visao", estadoVisao);

        if (MD != null && ME != null) {
            telemetry.addData("Encoder MD / ME", "%d / %d",
                    MD.getCurrentPosition(), ME.getCurrentPosition());
        }

        telemetry.update();
    }

    private void pararTracao() {
        if (MD != null) {
            MD.setPower(0);
        }

        if (ME != null) {
            ME.setPower(0);
        }
    }

    private void pararMecanismos() {
        if (MI != null) {
            MI.setPower(0);
        }

        if (MS != null) {
            MS.setPower(0);
        }
    }

    private void pararTudo() {
        pararTracao();
        pararMecanismos();
    }

    private void fecharVisao() {
        if (visionPortal != null) {
            try {
                visionPortal.close();
            } catch (RuntimeException ignored) {
            } finally {
                visionPortal = null;
            }
        }
    }

    private enum Alianca {
        RED,
        BLUE
    }

    private enum LadoCampo {
        SCORING,
        AUDIENCE
    }

    private interface Acao {
        boolean executar();
    }

    private static final class Tarefa {
        private final String nome;
        private final Acao acao;

        private Tarefa(String nome, Acao acao) {
            this.nome = nome;
            this.acao = acao;
        }
    }

    private static final class AlvoVisao {
        private final String chave;
        private final String nome;
        private final String tipo;
        private final double range;
        private final double bearing;
        private final double yaw;
        private final double roll;

        private AlvoVisao(
                String chave,
                String nome,
                String tipo,
                double range,
                double bearing,
                double yaw,
                double roll) {
            this.chave = chave;
            this.nome = nome;
            this.tipo = tipo;
            this.range = range;
            this.bearing = bearing;
            this.yaw = yaw;
            this.roll = roll;
        }
    }

    private static final class ControladorPID {
        private final double kp;
        private final double ki;
        private final double kd;
        private final double limiteIntegral;
        private final double tempoFiltroDerivada;

        private double integral;
        private double erroAnterior;
        private double derivadaFiltrada;
        private boolean primeiraAmostra = true;

        private ControladorPID(
                double kp,
                double ki,
                double kd,
                double limiteIntegral,
                double tempoFiltroDerivada) {
            this.kp = kp;
            this.ki = ki;
            this.kd = kd;
            this.limiteIntegral = limiteIntegral;
            this.tempoFiltroDerivada = tempoFiltroDerivada;
        }

        private double calcular(
                double erro,
                double dt,
                double saidaMinima,
                double saidaMaxima) {

            if (primeiraAmostra) {
                erroAnterior = erro;
                primeiraAmostra = false;
            }

            double derivada = (erro - erroAnterior) / dt;
            double alpha = dt / (tempoFiltroDerivada + dt);
            derivadaFiltrada += alpha * (derivada - derivadaFiltrada);

            double integralAnterior = integral;
            integral = limitarEstatico(
                    integral + erro * dt,
                    -limiteIntegral,
                    limiteIntegral
            );

            double saidaLivre =
                    kp * erro
                            + ki * integral
                            + kd * derivadaFiltrada;

            double saida = limitarEstatico(
                    saidaLivre,
                    saidaMinima,
                    saidaMaxima
            );

            boolean saturadoNoSentidoDoErro =
                    saida != saidaLivre
                            && Math.signum(saidaLivre) == Math.signum(erro);

            if (saturadoNoSentidoDoErro) {
                integral = integralAnterior;
            }

            erroAnterior = erro;
            return saida;
        }

        private double getVelocidadeYaw() {
            return -derivadaFiltrada;
        }

        private static double limitarEstatico(
                double valor,
                double minimo,
                double maximo) {
            return Math.max(minimo, Math.min(maximo, valor));
        }
    }
}
