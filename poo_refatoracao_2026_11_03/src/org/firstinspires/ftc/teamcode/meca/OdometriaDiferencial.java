package org.firstinspires.ftc.teamcode.meca;

import com.qualcomm.robotcore.hardware.DcMotor;

/** Integra a distancia dos encoders com o rumo informado pela IMU.
 *  A IMU e a origem da ORIENTACAO; encoders nao medem posicao absoluta.
 *  A convencao de sinais coincide com a odometria das fontes L1/L2.
 */
public final class OdometriaDiferencial {
    private final DcMotor esquerdo;
    private final DcMotor direito;
    private final double ticksPorMetro;
    private int ticksEsquerdoAnterior;
    private int ticksDireitoAnterior;
    private double rumoAnterior;
    private PoseMequinha pose;
    private boolean inicializada;

    public OdometriaDiferencial(DcMotor esquerdo, DcMotor direito,
                              double ticksPorMetro) {
        if (esquerdo == null || direito == null
                || !(ticksPorMetro > 0.0) || !Double.isFinite(ticksPorMetro)) {
            throw new IllegalArgumentException("Argumentos invalidos da odometria");
        }
        this.esquerdo = esquerdo;
        this.direito = direito;
        this.ticksPorMetro = ticksPorMetro;
    }

    public void iniciar(PoseMequinha poseInicial) {
        if (poseInicial == null) throw new IllegalArgumentException("Pose inicial ausente");
        pose = poseInicial;
        rumoAnterior = poseInicial.rumoGraus;
        ticksEsquerdoAnterior = esquerdo.getCurrentPosition();
        ticksDireitoAnterior = direito.getCurrentPosition();
        inicializada = true;
    }

    public PoseMequinha atualizar(double rumoAtualGraus) {
        if (!inicializada) throw new IllegalStateException("Use iniciar() primeiro");
        int ticksEsquerdo = esquerdo.getCurrentPosition();
        int ticksDireito = direito.getCurrentPosition();
        double deltaE = (ticksEsquerdo - ticksEsquerdoAnterior) / ticksPorMetro;
        double deltaD = (ticksDireito - ticksDireitoAnterior) / ticksPorMetro;
        double distanciaCentro = 0.5 * (deltaE + deltaD);
        double variacaoRumo = PoseMequinha.normalizarGraus(rumoAtualGraus - rumoAnterior);
        double rumoMedio = rumoAnterior + variacaoRumo / 2.0;
        double radians = Math.toRadians(rumoMedio);
        pose = new PoseMequinha(
                pose.x - Math.sin(radians) * distanciaCentro,
                pose.y + Math.cos(radians) * distanciaCentro,
                rumoAtualGraus);
        ticksEsquerdoAnterior = ticksEsquerdo;
        ticksDireitoAnterior = ticksDireito;
        rumoAnterior = PoseMequinha.normalizarGraus(rumoAtualGraus);
        return pose;
    }

    public PoseMequinha pose() {
        if (!inicializada) throw new IllegalStateException("Use iniciar() primeiro");
        return pose;
    }
}
