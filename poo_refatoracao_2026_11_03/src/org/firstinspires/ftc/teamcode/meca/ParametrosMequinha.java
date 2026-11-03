package org.firstinspires.ftc.teamcode.meca;

/** Medidas de referencia do Mequinha, herdadas dos autônomos L1/L2 no Meca Sim.
 *  Nao representa calibracao fisica do shooter nem certificado de largada legal.
 */
public final class ParametrosMequinha {
    private ParametrosMequinha() { }

    public static final double DIAMETRO_RODA_M = 0.090;
    public static final double TICKS_POR_VOLTA_RODA = 560.0;
    public static final double BITOLA_M = 0.450;
    public static final double CHASSI_M = 0.450;
    public static final double ARENA_M = 3.660;
    public static final double SERVO_FECHADO = 0.20;
    public static final double SERVO_ABERTO = 0.70;

    public static double ticksPorMetro() {
        return TICKS_POR_VOLTA_RODA / (Math.PI * DIAMETRO_RODA_M);
    }
}
