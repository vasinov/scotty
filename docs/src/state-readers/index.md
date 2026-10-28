# State Readers

State readers inspect a simulated state without causing measurement. They require an implicit `QuantumContext` to compute probabilities or density matrices.

## Basis-state probabilities

```scala mdoc:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator

implicit val simulator: QuantumContext = QuantumSimulator()
val circuit = Circuit(X(0), H(1), CNOT(0, 1))
val state = simulator.run(circuit)
println(StateProbabilityReader(state).toHumanString)
```

The nonzero basis states are `01` and `11`, each with probability 50%:

```text
01: Amplitude: 0.707+0.000i, P: 50.00%
11: Amplitude: 0.707+0.000i, P: 50.00%
```

```scala mdoc:invisible
assert(StateProbabilityReader(state).toHumanString.linesIterator.toSet == Set(
  "01: Amplitude: 0.707+0.000i, P: 50.00%",
  "11: Amplitude: 0.707+0.000i, P: 50.00%"
))
```

`StateProbabilityReader.read` returns structured `StateData` values with a basis string, complex amplitude and probability. Zero-probability basis states are omitted by the simulator.

## Individual qubit probabilities

```scala mdoc:silent
val qubits = QubitProbabilityReader(state).read
println(qubits.map(_.toHumanString).mkString("\n"))
```

```text
qubit_0: P(0) = 0.00% P(1) = 100.00%
qubit_1: P(0) = 50.00% P(1) = 50.00%
```

```scala mdoc:invisible
assert(qubits.map(_.toHumanString) == Seq(
  "qubit_0: P(0) = 0.00% P(1) = 100.00%",
  "qubit_1: P(0) = 50.00% P(1) = 50.00%"
))
```

In `0.6.0`, `QubitProbabilityReader.toHumanString` calls the case class's default `toString`; it does not use `QubitData.toHumanString`. Format `read` results individually as above for percentage output.

Pass a register to attach labels to reader indices:

```scala mdoc:silent
val labeled = QubitRegister(Qubit.zero("index_0"), Qubit.zero("index_1"))
val reader = QubitProbabilityReader(labeled, state)
val first = reader.read("index_0")
assert(first.exists(q => scala.math.abs(q.probabilityOfOne - 1.0) < 0.00001))
```

Reader labels use register sequence positions. Register initialization uses the opposite ordering to gate indices for multi-qubit states; see the [ordering caveat](../circuits-and-qubits/index.md#register-ordering). The labels above explicitly describe reader indices.

Individual probabilities do not retain phase information or capture all correlations between qubits. Use the full basis amplitudes when those properties matter.

## Bloch sphere

`BlochSphereReader` accepts only a **single-qubit** state. It returns angles in radians and coordinates; `toHumanString` formats the angles in degrees:

```scala mdoc:silent
val phased = simulator.run(Circuit(H(0), PHASE(Math.PI / 8, 0)))
println(BlochSphereReader(phased).toHumanString)
```

```text
phi: 22.50°, theta: 90.00°, x: 0.92, y: 0.38, z: 0.00
```

```scala mdoc:invisible
assert(BlochSphereReader(phased).toHumanString ==
  "phi: 22.50°, theta: 90.00°, x: 0.92, y: 0.38, z: 0.00")
```

In this release the azimuth is computed with `acos`, so it cannot distinguish positive and negative azimuths. Consult the returned coordinates when the phase quadrant matters.
