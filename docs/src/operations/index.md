# Operations

An `Op` references qubits by numeric gate index. Scotty provides `Gate` and `Measure` operations.

## Gates

Gates transform the state vector. Their order in a circuit matters. See [Standard Gates](../standard-gates/index.md) and [Modifiers and Custom Gates](../modifiers-and-custom-gates/index.md).

## Measurements

```scala mdoc:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator

val simulator = QuantumSimulator()
val circuit = Circuit(H(0), CNOT(0, 1), Measure(0))
val result: State = simulator.run(circuit)
val description = result match {
  case _: Superposition => "not measured"
  case c: Collapsed => s"measured: ${c.toBinary}"
}
```

Although `Measure(0)` names one qubit, `QuantumSimulator` detects the presence of any measurement, applies **all gates**, then samples the **whole register**. The position and target of `Measure` do not introduce an intermediate or partial measurement.

For example, the following returns `"1"` deterministically, because `X(0)` runs before measurement:

```scala mdoc:silent
val deferred = simulator.run(Circuit(Measure(0), X(0)))
assert(deferred.asInstanceOf[Collapsed].toBinary == "1")
```

For a final whole-register measurement, prefer the explicit method:

```scala mdoc:silent
val measured: Collapsed = simulator.runAndMeasure(Circuit(H(0), CNOT(0, 1)))
assert(Set("00", "11").contains(measured.toBinary))
```

Do not use these operations to model protocols requiring intermediate measurement results to control subsequent gates.
