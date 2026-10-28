# Circuits and Qubits

A `Circuit` contains a `QubitRegister` and a sequence of operations. By default the register contains enough zero-state qubits for the highest operation index: `Circuit(H(5))` creates six qubits.

```scala mdoc:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.quantum.math.Complex
import scotty.simulator.QuantumSimulator

implicit val simulator: QuantumContext = QuantumSimulator()
val circuit = Circuit(H(0)).combine(Circuit(CNOT(0, 1)))
val indices = circuit.indices
assert(indices == (0 until 2))
```

`combine` concatenates operations and generates a new default register. **It does not preserve a custom register** from either circuit. Apply `withRegister` after combining:

```scala mdoc:silent
val initialized = circuit.withRegister("10")
val sameBasisState = circuit.withRegister(Qubit.one, Qubit.zero)
```

## Qubit states

A qubit has complex amplitudes $\alpha$ and $\beta$:

$$
\lvert\psi\rangle = \alpha\lvert0\rangle + \beta\lvert1\rangle,
\qquad |\alpha|^2 + |\beta|^2 = 1.
$$

The amplitudes are `Qubit.a` and `Qubit.b`. Their squared magnitudes give measurement probabilities.

```scala mdoc:silent
val message = Qubit(Complex(0.8), Complex(0.6), "message")
val register = QubitRegister(Qubit.one, Qubit.fiftyFifty, Qubit.zero)
val basisFromString = QubitRegister("1001")
val basisFromInteger = QubitRegister(9)
assert(basisFromString == basisFromInteger)
```

Integer initialization uses the integer's binary representation without leading zeros. Use a string such as `"0001"` when register width matters. A custom register must be large enough for every operation index.

## Register ordering

Displayed basis strings use the usual binary notation: **gate index zero is the rightmost bit**. For example:

```scala mdoc:silent
val rightmostFlip = simulator.runAndMeasure(Circuit(X(0)).withRegister("00"))
assert(rightmostFlip.toBinary == "01")
val initializedBits = simulator.runAndMeasure(Circuit(I(0)).withRegister("10"))
assert(initializedBits.toBinary == "10")
```

In `0.6.0`, register construction and gate indexing use different orders: `QubitRegister.values` and explicit qubit arguments are stored **left to right**, while gates index the state vector's bits **right to left**. In a two-qubit register, `QubitRegister(Qubit.one, Qubit.zero)` initializes `"10"`, so the first constructor argument is the initial state acted on by gate index `1`.

Qubit labels must be unique, but reader label lookup uses the register's sequence index. For asymmetric multi-qubit registers, labels can therefore appear attached to the opposite bit from their initialization position. Prefer numeric gate indices and `toBinary` when verifying ordering; labels do not change gate addressing. See [State Readers](../state-readers/index.md).

Circuits and qubits are immutable containers. A `Superposition`, however, contains a mutable array; its `applyGate` method changes that array in place.
