package scottyjava.quantum.bit;

import scottyjava.Labeled;

public abstract class Bit extends Labeled<String> {
    public static Bit fromString(String number) {
        if (number.equals("1")) return new One();
        else return new Zero();
    }

    public abstract int toInt();
}
