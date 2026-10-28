# Getting Started

## Installation

Add Scotty to your `build.sbt`:

```scala
libraryDependencies += "xyz.entangled" %% "scotty" % "0.6.0"
```

Use Scala **2.12 or 2.13**. For Maven or Gradle, select the artifact matching your Scala binary version: [scotty_2.13](https://central.sonatype.com/artifact/xyz.entangled/scotty_2.13/0.6.0) or [scotty_2.12](https://central.sonatype.com/artifact/xyz.entangled/scotty_2.12/0.6.0). The organization remains `xyz.entangled` even though the repository has moved to `vasinov/scotty`.

For example, Maven with Scala 2.13 uses:

```xml
<dependency>
  <groupId>xyz.entangled</groupId>
  <artifactId>scotty_2.13</artifactId>
  <version>0.6.0</version>
</dependency>
```

## Create a Bell pair

This complete example prepares two entangled qubits. All qubits start in $\lvert0\rangle$ unless you supply a register.

```scala mdoc:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator

implicit val simulator: QuantumContext = QuantumSimulator()
val bell = Circuit(H(0), CNOT(0, 1))
val state = simulator.run(bell)
println(StateProbabilityReader(state).toHumanString)
```

The two possible basis states have equal probabilities:

```text
00: Amplitude: 0.707+0.000i, P: 50.00%
11: Amplitude: 0.707+0.000i, P: 50.00%
```

```scala mdoc:invisible
val probabilities = StateProbabilityReader(state).read.map(p => p.state -> p.probability).toMap
assert(probabilities.keySet == Set("00", "11"))
assert(probabilities.values.forall(p => scala.math.abs(p - 0.5) < 0.00001))
assert(StateProbabilityReader(state).toHumanString.linesIterator.toSet == Set(
  "00: Amplitude: 0.707+0.000i, P: 50.00%",
  "11: Amplitude: 0.707+0.000i, P: 50.00%"
))
```

`QuantumSimulator` implements `QuantumContext`. Making the context implicit lets the state readers use it. The simulator returns `Superposition` when the circuit has no measurement operations.

To see individual qubit probabilities, format each reader result:

```scala mdoc:silent
println(QubitProbabilityReader(state).read.map(_.toHumanString).mkString("\n"))
```

```text
qubit_0: P(0) = 50.00% P(1) = 50.00%
qubit_1: P(0) = 50.00% P(1) = 50.00%
```

These marginal probabilities alone do not prove entanglement; the joint state above shows the correlation between the two qubits.

## Measure the circuit

```scala mdoc:silent
val measured = simulator.runAndMeasure(bell)
println(measured.toBinary)
```

Each run returns either `00` or `11`. Repeated runs approach a 50/50 split; an individual run is random.

```scala mdoc:invisible
assert(Set("00", "11").contains(measured.toBinary))
```

## Set the initial state

```scala mdoc:silent
val initialized = bell.withRegister("10")
```

Binary strings are written with the most significant bit on the left. Gate index `0` acts on the **rightmost** bit of the displayed basis state. See [register ordering](../circuits-and-qubits/index.md#register-ordering) for the distinction between gate indices and register sequence positions in this release.

Continue with [Quantum Context and Simulator](../quantum-context-and-simulator/index.md).
