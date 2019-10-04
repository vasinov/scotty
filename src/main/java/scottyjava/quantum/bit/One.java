package scottyjava.quantum.bit;

import scottyjava.quantum.math.Complex;

public class One extends Bit {
    public int toInt() {
        return 1;
    }

    public Complex[] toComplexArray() {
        return new Complex[] { new Complex(0), new Complex(1) };
    }

    public float[] toVector() {
        return new float[] { 0f, 0f, 1f, 0f };
    }
}
