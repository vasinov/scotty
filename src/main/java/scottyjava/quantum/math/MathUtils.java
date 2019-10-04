package scottyjava.quantum.math;

import scottyjava.quantum.bit.Bit;
import scottyjava.quantum.bit.Zero;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MathUtils {
    public static final double PRECISION = 1e6;

    public static double toPercent(double d) {
        return d * 100;
    }

    public static boolean approxEqual(double d1, double d2) {
        return Math.abs(d1 - d2) < 1 / PRECISION;
    }

    public static List<Bit> toBinary(int number) {
        return Integer
                .toBinaryString(number)
                .chars()
                .mapToObj(c -> Bit.fromString(String.valueOf((char) c)))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Integer> bitsToInts(List<Bit> ints) {
        return ints.stream().mapToInt(Bit::toInt).boxed().collect(Collectors.toList());
    }

    public static List<Bit> toPaddedBinary(int n, int qubitCount) {
        var bits = toBinary(n);
        var paddedBits = new ArrayList<Bit>(Collections.nCopies(qubitCount - bits.size(), new Zero()));

        paddedBits.addAll(bits);

        return paddedBits;
    }

    public static List<Integer> toPaddedBinaryInts(int n, int qubitCount) {
        return bitsToInts((toPaddedBinary(n, qubitCount)));
    }

    public static String toPaddedBinaryString(int n, int qubitCount) {
        return toPaddedBinaryInts(n, qubitCount).stream().map(String::valueOf).collect(Collectors.joining(""));
    }

    public static boolean isProbabilityValid(double a, double b) {
        var sumOfSquares = Math.pow(a, 2) + Math.pow(b, 2);

        return Math.abs(sumOfSquares - 1) < 1 / PRECISION;
    }
}
