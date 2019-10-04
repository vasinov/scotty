package scottyjava.quantum.bit;

import scottyjava.quantum.math.Complex;

public class Zero extends Bit {
    public int toInt() {
        return 0;
    }

    public Complex[] toComplexArray() {
        return new Complex[] { new Complex(1), new Complex(0) };
    }

    public float[] toVector() {
        return new float[] { 1f, 0f, 0f, 0f };
    }
}
