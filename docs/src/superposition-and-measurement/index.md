# Superposition and Measurement

An $n$-qubit pure state contains $2^n$ complex amplitudes:

$$
\lvert\psi\rangle = \sum_{k=0}^{2^n-1} c_k\lvert k\rangle,
\qquad \sum_{k=0}^{2^n-1}|c_k|^2=1.
$$

The probability of measuring basis state $k$ is $|c_k|^2$. An amplitude itself is not a probability: its complex phase affects interference.

## Superposition

`Superposition(register, state)` stores the original register and the current state vector. For three qubits, entries `0` and `1` contain the real and imaginary parts of the amplitude of `000`; entries `2` and `3` contain the amplitude of `001`; and so on through `111`.

Use a state reader for meaningful output. `Superposition.toHumanString` prints the raw float vector.

```scala mdoc:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator

implicit val simulator: QuantumContext = QuantumSimulator()
val state = simulator.run(Circuit(H(0)))
println(StateProbabilityReader(state).toHumanString)
```

`Superposition.applyGate` mutates the vector and requires an implicit `QuantumContext`. `combine` computes a tensor product; when constructing larger states directly, ensure the associated register matches the combined vector.

## Collapsed state

`Collapsed(register, index)` represents a sampled classical result. `toBinary` returns a fixed-width basis string. For example, index `6` in a three-qubit register is `"110"`.

```scala mdoc:silent
val measured = simulator.runAndMeasure(Circuit(X(1), X(2)))
assert(measured.toBinary == "110")
val bits: BitRegister = measured.toBitRegister
```

`BitRegister` contains `Zero` and `One` values. Binary strings are the clearest way to inspect a multi-qubit measurement; see the [label and ordering caveat](../circuits-and-qubits/index.md#register-ordering).

## Measurement behavior

`runAndMeasure` executes the circuit and samples the whole register. A circuit containing any `Measure` operation also returns `Collapsed` from `run`.

In this simulator, measurements happen **after all gates**, regardless of where the `Measure` operation appears. The entire register collapses, even when only one qubit is named in `Measure`. This does not implement general intermediate measurements, partial collapse, or classical feed-forward.

Read [Operations](../operations/index.md) for examples.
