package scottyjava.quantum.math;

public class Complex {
    public final float r;
    public final float i;

    public Complex(float r, float i) {
        this.r = r;
        this.i = i;
    }

    public Complex(float r) { this( r, 0); }
    public Complex(double r, double i) { this((float) r, (float) i); }
    public Complex(double r) { this((float) r, 0); }

    public static float[] product(float r1, float i1, float r2, float i2) {
        return new float[] {
                r1 * r2 - i1 * i2,
                r1 * i2 + i1 * r2
        };
    }

    public static float[] product(float[] c1, float[] c2) {
        return product(c1[0], c1[1], c2[0], c2[1]);
    }

    public static float[] sum(float r1, float i1, float r2, float i2) {
        return new float[] {
                r1 +r2,
                i1 + i2
        };
    }

    public static float[] sum(float[] c1, float[] c2) {
        return sum(c1[0], c1[1], c2[0], c2[1]);
    }

    public static Complex e(double phi) {
        return new Complex(Math.cos(phi), Math.sin(phi));
    }

    public static float abs(float r, float i) {
        return (float) Math.sqrt(Math.pow(r, 2) + Math.pow(i, 2));
    }

    public static float abs(float[] c) {
        return abs(c[0], c[1]);
    }

    public static float abs(Complex c) {
        return abs(c.r, c.i);
    }

    public float abs() {
        return abs(r, i);
    }

    public String toHumanString() {
        var real = String.format("%.3f", r);
        var sign = (i >= 0) ? "+" : "";
        var im = String.format("%.3fi", i);

        return real + sign + im;
    }
}
