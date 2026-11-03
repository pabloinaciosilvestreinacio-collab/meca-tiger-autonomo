package org.firstinspires.ftc.teamcode.meca;

/** Pose imutavel no mapa: coordenadas em metros e rumo em graus.
 *  Convenção histórica: rumo zero aponta para +Y; 90 graus para -X.
 */
public final class PoseMequinha {
    public final double x;
    public final double y;
    public final double rumoGraus;

    public PoseMequinha(double x, double y, double rumoGraus) {
        if (!Double.isFinite(x) || !Double.isFinite(y)
                || !Double.isFinite(rumoGraus)) {
            throw new IllegalArgumentException("Pose deve conter valores finitos");
        }
        this.x = x;
        this.y = y;
        this.rumoGraus = normalizarGraus(rumoGraus);
    }

    public double distanciaA(PoseMequinha outro) {
        return Math.hypot(outro.x - x, outro.y - y);
    }

    public static double normalizarGraus(double graus) {
        if (!Double.isFinite(graus)) {
            throw new IllegalArgumentException("Angulo invalido");
        }
        double r = graus % 360.0;
        if (r > 180.0) r -= 360.0;
        if (r < -180.0) r += 360.0;
        return r;
    }
}
