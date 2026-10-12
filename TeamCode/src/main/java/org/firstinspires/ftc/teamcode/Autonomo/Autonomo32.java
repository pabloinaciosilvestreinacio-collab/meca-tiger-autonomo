package org.firstinspires.ftc.teamcode.Autonomo;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@Autonomous(name = "Autonomo 3.2", group = "Teste")
public class Autonomo32 extends LinearOpMode {

    private DcMotor MD;
    private DcMotor ME;
    private DcMotor MI;
    private DcMotor MS;
    private IMU imu;

    private final ElapsedTime cronometroAuto = new ElapsedTime();

    private String etapaAtual = "Inicializacao";
    private String motivoFalha = "";
    private double rumoDoDeslocamento;

    // Parametros da estrategia: altere estes valores durante os testes de campo.
    private static final double ANGULO_COLETA = -50.0;
    private static final double DISTANCIA_COLETA_MM = 2000.0;
    private static final double VELOCIDADE_COLETA = 0.35;
    private static final double ANGULO_LANCAMENTO = 100.0;
    private static final int QUANTIDADE_LANCAMENTOS = 3;

    private static final double TICKS_POR_REV_MOTOR = 28.0;
    private static final double REDUCAO = 20.0;
    private static final double FATOR_CALIBRACAO_DISTANCIA = 1.0;
    private static final double DIAMETRO_RODA_MM = 90.0;
    private static final double TICKS_POR_MM =
            (TICKS_POR_REV_MOTOR * REDUCAO)
                    / (Math.PI * DIAMETRO_RODA_MM)
                    * FATOR_CALIBRACAO_DISTANCIA;

    private static final double KP_VIRADA = 0.012;
    private static final double KI_VIRADA = 0.00012;
    private static final double KD_VIRADA = 0.0015;

    private static final double KP_RUMO = 0.018;
    private static final double KI_RUMO = 0.00015;
    private static final double KD_RUMO = 0.0012;

    private static final double LIMITE_INTEGRAL = 250.0;
    private static final double TEMPO_FILTRO_DERIVADA = 0.08;

    private static final double MIN_POTENCIA_VIRADA = 0.08;
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
    private static final double TEMPO_SEM_MELHORA_GIRO = 1.20;
    private static final double MELHORA_MINIMA_GIRO_GRAUS = 1.0;

    private static final int PROGRESSO_MINIMO_TICKS = 6;
    private static final double TEMPO_SEM_PROGRESSO = 1.2;
    private static final int LIMITE_ENCODER_NEGATIVO = -10;

    private static final double POTENCIA_INTAKE = 0.60;
    private static final double POTENCIA_LANCAMENTO = 0.70;
    private static final double TEMPO_ACELERACAO_LANCADOR = 0.80;
    private static final double TEMPO_ALIMENTACAO_POR_PECA = 0.45;
    private static final double TEMPO_ENTRE_PECAS = 0.15;

    private static final double TEMPO_LIMITE_VIRADA = 7.0;
    private static final double TEMPO_LIMITE_DISTANCIA = 12.0;
    private static final double TEMPO_MAXIMO_AUTO = 30.0;
    private static final double RESERVA_SEGURANCA = 1.0;
    private static final double MARGEM_INICIO_LANCAMENTO = 0.35;

    @Override
    public void runOpMode() {
        try {
            configurarHardware();

            boolean imuInicializado = configurarImu();
            reiniciarEncoders();

            telemetry.addLine("Meca Tiger - Autonomo 3.2");
            telemetry.addData("IMU inicializado", imuInicializado);
            telemetry.addData("Ticks/mm", "%.4f", TICKS_POR_MM);
            telemetry.addData("Reducao (nao aferida)", "%.2f", REDUCAO);
            telemetry.addLine("-50, coletar 2000 mm, +100, lancar 3");
            telemetry.update();

            waitForStart();

            if (isStopRequested()) {
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
                    "Autonomo 3.2",
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
                    tarefa("Lancar pecas",
                            () -> lancarPecasPorTempo(
                                    QUANTIDADE_LANCAMENTOS))
            );

            if (!sucesso) {
                mostrarFalha();
                return;
            }

            pararTudo();

            telemetry.addLine("Autonomo 3.2 concluido");
            telemetry.addData("Ultima etapa", etapaAtual);
            telemetry.addData("Tempo total", "%.2f s", cronometroAuto.seconds());
            telemetry.addData("Yaw final", "%.2f", lerYaw());
            telemetry.addData("Encoder MD", MD.getCurrentPosition());
            telemetry.addData("Encoder ME", ME.getCurrentPosition());
            telemetry.update();

        } catch (RuntimeException e) {
            etapaAtual = "Falha de inicializacao ou runtime";
            motivoFalha = e.getClass().getSimpleName();
            mostrarFalha();
        } finally {
            pararTudo();
        }
    }

    private void configurarHardware() {
        MD = hardwareMap.get(DcMotor.class, "MD");
        ME = hardwareMap.get(DcMotor.class, "ME");
        MI = hardwareMap.get(DcMotor.class, "MI");
        MS = hardwareMap.get(DcMotor.class, "MS");
        imu = hardwareMap.get(IMU.class, "imu");

        MD.setDirection(DcMotor.Direction.REVERSE);
        ME.setDirection(DcMotor.Direction.FORWARD);

        MD.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        ME.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        MI.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        MS.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        pararTudo();
    }

    private boolean configurarImu() {
        RevHubOrientationOnRobot orientacao =
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                );

        try {
            return imu.initialize(new IMU.Parameters(orientacao));
        } catch (RuntimeException e) {
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
                TEMPO_LIMITE_VIRADA);
    }

    private boolean virarParaRumo(double rumoAlvo, double limiteTempo) {
        if (!Double.isFinite(rumoAlvo)
                || !Double.isFinite(limiteTempo)
                || limiteTempo <= 0.0) {
            motivoFalha = "Parametros de giro invalidos";
            pararTracao();
            return false;
        }

        double tempoEstavelDesde = -1.0;
        double menorErroAbs = Double.POSITIVE_INFINITY;
        ElapsedTime semMelhora = new ElapsedTime();

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
                && tempoDisponivelMovimento()
                && timer.seconds() < limiteTempo) {

            double rumoAtual = lerYaw();

            if (!Double.isFinite(rumoAtual)) {
                motivoFalha = "IMU invalido durante virada";
                pararTracao();
                return false;
            }

            double erro = normalizarAngulo(rumoAlvo - rumoAtual);

            if (Math.abs(erro) + MELHORA_MINIMA_GIRO_GRAUS < menorErroAbs) {
                menorErroAbs = Math.abs(erro);
                semMelhora.reset();
            }

            if (Math.abs(erro) > 10.0
                    && semMelhora.seconds() > TEMPO_SEM_MELHORA_GIRO) {
                motivoFalha = "Giro sem progresso: verificar MD, ME e yaw";
                pararTracao();
                return false;
            }

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

            if (Math.abs(erro) <= TOLERANCIA_VIRADA) {
                comando = 0.0;
            } else if (Math.abs(comando) < MIN_POTENCIA_VIRADA) {
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
                ? (tempoDisponivelMovimento()
                        ? "Tempo limite da virada"
                        : "Reserva do lancamento atingida durante giro")
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
                || !Double.isFinite(TICKS_POR_MM)
                || TICKS_POR_MM <= 0.0
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
                && tempoDisponivelMovimento()
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

                if (Math.abs(erroRumo) <= TOLERANCIA_RUMO_DESLOCAMENTO) {
                    return true;
                }

                boolean corrigido = virarParaRumo(
                        rumoAlvo, TEMPO_CORRECAO_FINAL_RUMO);
                if (!corrigido && "Tempo limite da virada".equals(motivoFalha)) {
                    motivoFalha = "Tempo limite na correcao final do rumo";
                }
                return corrigido;
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
                ? (tempoDisponivelMovimento()
                        ? "Tempo limite do deslocamento"
                        : "Reserva do lancamento atingida no percurso")
                : "Tempo AUTO esgotado ou STOP";
        return false;
    }

    private boolean coletarDuranteDeslocamento(
            double distanciaMm,
            double rumoAlvo,
            double velocidade) {

        if (!tempoDisponivelMovimento()) {
            motivoFalha = "Sem tempo seguro para iniciar coleta";
            return false;
        }

        MI.setPower(POTENCIA_INTAKE);

        try {
            return andarComRumo(distanciaMm, rumoAlvo, velocidade);
        } finally {
            MI.setPower(0);
        }
    }

    private boolean lancarPecasPorTempo(int quantidade) {
        if (quantidade <= 0) {
            motivoFalha = "Quantidade invalida";
            return false;
        }

        double tempoNecessario = calcularTempoLancamento(quantidade);

        if (!tempoDisponivel()
                || tempoRestanteSeguro()
                <= tempoNecessario + MARGEM_INICIO_LANCAMENTO) {
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

        return opModeIsActive() && tempoDisponivel()
                && timer.seconds() >= segundos;
    }

    private void reiniciarEncoders() {
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
        return Math.max(0.0, TEMPO_MAXIMO_AUTO - RESERVA_SEGURANCA
                - cronometroAuto.seconds());
    }

    private boolean tempoDisponivelMovimento() {
        return tempoDisponivel()
                && tempoRestanteSeguro()
                        > calcularTempoLancamento(QUANTIDADE_LANCAMENTOS)
                                + MARGEM_INICIO_LANCAMENTO;
    }

    private double normalizarAngulo(double graus) {
        if (!Double.isFinite(graus)) {
            return Double.NaN;
        }

        double angulo = graus % 360.0;
        if (angulo > 180.0) {
            angulo -= 360.0;
        } else if (angulo < -180.0) {
            angulo += 360.0;
        }
        return angulo;
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

            double diferencaErro = (erro - erroAnterior + 540.0) % 360.0 - 180.0;
            double derivada = diferencaErro / dt;
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
