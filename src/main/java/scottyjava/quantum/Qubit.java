package scottyjava.quantum;

import scottyjava.quantum.math.MathUtils;
import scottyjava.Labeled;
import scottyjava.quantum.bit.Bit;
import scottyjava.quantum.bit.Zero;
import scottyjava.quantum.math.Complex;
import java.util.Optional;

public class Qubit extends Labeled<String> {
    public final Complex a;
    public final Complex b;

    public Qubit(Complex a, Complex b, Optional<String> label) {
        this.a = a;
        this.b = b;
        this.label = label;
    }

    public Qubit(Complex a, Complex b) { this(a, b, Optional.empty()); }
    public Qubit(Complex[] cs) { this(cs[0], cs[1]); }

    public static Qubit fromBit(Bit bit) {
        if (bit instanceof Zero) {
            return Qubit.zero();
        } else {
            return Qubit.one();
        }
    }

    public static Qubit zero(Optional<String> label) {
        return new Qubit(new Complex(1), new Complex(0), label);
    }

    public static Qubit zero(String label) {
        return Qubit.zero(Optional.of(label));
    }

    public static Qubit zero() {
        return Qubit.zero(Optional.empty());
    }

    public static Qubit one(Optional<String> label) {
        return new Qubit(new Complex(0), new Complex(1), label);
    }

    public static Qubit one(String label) {
        return Qubit.one(Optional.of(label));
    }

    public static Qubit one() {
        return Qubit.one(Optional.empty());
    }

    public static Qubit fiftyFifty(Optional<String> label) {
        return new Qubit(new Complex(1 / Math.sqrt(2.0)), new Complex(1 / Math.sqrt(2.0)), label);
    }

    public static Qubit fiftyFifty(String label) {
        return Qubit.fiftyFifty(Optional.of(label));
    }

    public static Qubit fiftyFifty() {
        return Qubit.fiftyFifty(Optional.empty());
    }

    public boolean areAmplitudesValid() {
        return MathUtils.isProbabilityValid(Complex.abs(a), Complex.abs(b));
    }

    public float[] toVector() {
        return new float[] { a.r, a.i, b.r, b.i };
    }

    public String toHumanString() {
        return String.format("Qubit(%s, %s)", a.toString(), b.toString());
    }

    public double probabilityOfZero() {
        return Math.pow(a.abs(), 2);
    }

    public double probabilityOfOne() {
        return Math.pow(b.abs(), 2);
    }
}
