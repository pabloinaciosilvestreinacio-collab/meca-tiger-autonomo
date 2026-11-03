package org.firstinspires.ftc.teamcode.meca;

/**
 * AJUSTES DO MEQUINHA - POO, ETAPA 02 (sem alterar autonomos existentes).
 *
 * Ideia: os proximos OpModes POO consultarao ESTES nomes em vez de
 * espalhar numeros pelo codigo. POR ENQUANTO este arquivo e independente:
 * editar aqui NAO muda automaticamente os 8 OpModes antigos.
 *
 * Unidade de tempo: segundos. Coordenadas: metros. Angulos: graus.
 * Power e posicao de Servo: 0.0 a 1.0, quando nao assinalados.
 *
 * ATENCAO: RPM de 127 e ticks do shooter de 560 sao de Meca Sim,
 * NAO configuracao validada no robo fisico.
 */
public final class AjustesAutonomoMequinha {

    private AjustesAutonomoMequinha() { }

    // =============================================================
    // 1) MECANISMOS - ALTERAR APENAS APOS UM TESTE DE CADA VEZ
    // =============================================================

    // Posicoes do servo ficam em ParametrosMequinha, sem duplicar.
    public static final double POSICAO_FECHADA =
            ParametrosMequinha.SERVO_FECHADO;    // 0.20
    public static final double POSICAO_ABERTA =
            ParametrosMequinha.SERVO_ABERTO;     // 0.70

    public static final double INTAKE_COLETA_POWER = 1.00;
    public static final double INTAKE_ALIMENTACAO_POWER = 1.00;
    public static final double SHOOTER_BASE_POWER_SIM = 0.60;

    // =============================================================
    // 2) CONTROLE RPM - PERFIL SIMULADOR, NAO COPIAR PARA HARDWARE
    // =============================================================

    public static final double SHOOTER_TICKS_VOLTA_SIM = 560.0;
    public static final double RPM_ALVO_SIM = 127.0;
    public static final double RPM_MINIMO_FATOR = 0.88;
    public static final double RPM_MAXIMO_FATOR = 1.10;
    public static final double RPM_KP = 0.0015;
    public static final double RPM_JANELA_S = 0.080;
    public static final int RPM_AMOSTRAS_ESTAVEIS = 3;
    public static final double RPM_WATCHDOG_ENCODER_S = 0.60;

    // =============================================================
    // 3) TEMPOS DO SERVO - PERFIS HISTORICOS DIFERENTES
    // =============================================================

    public static final double PULSO_NORMAL_S = 0.14;

    // L1-01, L1-02 e L1-03: primeiro posto L1.
    public static final double INTERVALO_L1_S = 0.82;

    // L1-03 e L1-04 v1/v2: quando disparam em L2.
    public static final double INTERVALO_L2_ANTIGO_S = 0.70;
    public static final double PRE_ALIMENTAR_L2_ANTIGO_S = 0.07;
    public static final double PULSO_ULTIMO_L2_ANTIGO_S = 0.25;
    public static final double LIMPEZA_L2_ANTIGO_S = 0.12;

    // L1-05 v17: segundo e terceiro postos de lançamento.
    public static final double INTERVALO_L105_L2_S = 0.84;
    public static final double PRE_ALIMENTAR_L105_L2_S = 0.09;
    public static final double PULSO_ULTIMO_L105_L2_S = 0.32;
    public static final double LIMPEZA_L105_L2_S = 0.16;

    // L2-01 v1 / L2-02 v2: valores embutidos no metodo original.
    public static final double INTERVALO_PARTIDA_L2_S = 0.84;
    public static final double PRE_ALIMENTAR_PARTIDA_L2_S = 0.09;
    public static final double PULSO_ULTIMO_PARTIDA_L2_S = 0.32;
    public static final double LIMPEZA_PARTIDA_L2_S = 0.16;

    // =============================================================
    // 4) TEMPO LIMITE - A ROTA DETERMINA O QUE E PRIORITARIO
    // =============================================================

    public static final double TEMPO_AUTONOMO_COM_PARK_S = 29.30;
    public static final double TEMPO_L105_SEM_PARK_S = 29.95;
    public static final double RESERVA_PARK_L201_S = 5.00;
    public static final double RESERVA_PARK_L202_S = 4.00;

    // =============================================================
    // 5) POSICAO - APENAS REFERENCIAS PROVISORIAS DO MECA SIM
    // =============================================================

    public static final PoseMequinha INICIO_L1_01 =
            new PoseMequinha(1.678, 0.399, 0.0);
    public static final PoseMequinha INICIO_L1_GERAL =
            new PoseMequinha(1.530, 0.255, 0.0);
    public static final PoseMequinha INICIO_L2_GERAL =
            new PoseMequinha(1.574, 3.420, 180.0);
    public static final PoseMequinha PARK_L1_01 =
            new PoseMequinha(0.350, 2.820, 90.0);
    public static final PoseMequinha PARK_PADRAO =
            new PoseMequinha(0.255, 2.380, 90.0);
    public static final PoseMequinha FLOWER_SUPERIOR =
            new PoseMequinha(1.300, 3.398, 0.0);

    // =============================================================
    // 6) CAMERA: NAO CONFIGURADA NESTA ETAPA
    // =============================================================
    // Sem imports de VisionPortal nem AprilTag para que as versoes
    // atuais continuem compilando no Meca Sim sem API de camera.
    // O modulo opcional de visao sera integrado e testado depois.
}
