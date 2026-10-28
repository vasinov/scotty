# Examples

Each example below runs independently. The documentation build compiles and executes it against the published `0.6.0` artifact.

## Interference

Two Hadamard gates restore a qubit to its initial zero state:

```scala mdoc:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator

val simulator = QuantumSimulator()
val result = simulator.runAndMeasure(Circuit(H(0), H(0)))
assert(result.toBinary == "0")
```

## A three-qubit GHZ state

```scala mdoc:reset:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator

implicit val simulator: QuantumContext = QuantumSimulator()
val ghz = Circuit(H(0), CNOT(0, 1), CNOT(1, 2))
val state = simulator.run(ghz)
val probabilities = StateProbabilityReader(state).read
assert(probabilities.map(_.state).toSet == Set("000", "111"))
assert(probabilities.forall(p => scala.math.abs(p.probability - 0.5) < 0.00001))
println(StateProbabilityReader(state).toHumanString)
```

Only `000` and `111` have nonzero probability. Sampling the circuit gives one of these two outcomes.

## Phase is invisible to a single basis measurement

```scala mdoc:reset:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator

implicit val simulator: QuantumContext = QuantumSimulator()
val plus = simulator.run(Circuit(H(0)))
val minus = simulator.run(Circuit(H(0), Z(0)))
val plusOne = QubitProbabilityReader(plus).read.head.probabilityOfOne
val minusOne = QubitProbabilityReader(minus).read.head.probabilityOfOne
assert(scala.math.abs(plusOne - minusOne) < 0.00001)

val plusAfterH = simulator.runAndMeasure(Circuit(H(0), H(0)))
val minusAfterH = simulator.runAndMeasure(Circuit(H(0), Z(0), H(0)))
assert(plusAfterH.toBinary == "0")
assert(minusAfterH.toBinary == "1")
```

Both intermediate states have 50% probability of one, but their relative phases differ. A final Hadamard makes that difference observable. Checking one-qubit probabilities alone is therefore insufficient to validate arbitrary-state teleportation.
