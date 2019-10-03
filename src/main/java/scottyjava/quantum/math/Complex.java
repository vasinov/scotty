package scottyjava.quantum.math;

public class Complex {
    public final float r;
    public final float i;

    public Complex(float r, float i) {
        this.r = r;
        this.i = i;
    }

    public String toHumanString() {
        var real = String.format("%.3f", r);
        var sign = (i >= 0) ? "+" : "";
        var im = String.format("%.3fi", i);

        return real + sign + im;
    }
}
