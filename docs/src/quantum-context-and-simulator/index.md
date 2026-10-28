# Quantum Context and Simulator

## Quantum context

`QuantumContext` defines the state operations used by Scotty. The built-in `QuantumSimulator` implements the trait on the CPU. A different backend would need to implement the same contract.

| Method | Purpose |
| --- | --- |
| `run(circuit)` | Execute gates and return a `State`. |
| `runAndMeasure(circuit)` | Execute the circuit and return a `Collapsed` state. |
| `runExperiment(circuit, trialsCount)` | Run independent trials and collect their measurements. |
| `measure(register, state)` | Sample a raw state vector. |
| `applyGate(state, gate)` | Mutate a state vector by applying a gate. |
| `probabilities(superposition)` | Read basis-state amplitudes and probabilities. |
| `tensorProduct(register, sp1, sp2)` | Combine state vectors. |
| `densityMatrix(vector)` | Compute the outer product of a state vector with its conjugate. |
| `isUnitary(targetGate)` | Check a single-qubit gate matrix. |

`QuantumContext.Vector` is `Array[Float]`; `QuantumContext.Matrix` is `Array[Array[Float]]`. Each complex number occupies consecutive real and imaginary entries. A two-element complex vector therefore occupies four floats.

## Simulator configuration

A seed makes a sequence of measurement draws reproducible within the same implementation:

```scala mdoc:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator
import scala.util.Random

val simulator = QuantumSimulator(new Random(42L))
val bell = Circuit(H(0), CNOT(0, 1))
val experiment = simulator.runExperiment(bell, 1000)
println(experiment.stateStats.toHumanString)
```

The output includes counts for `00`, `01`, `10` and `11`. Only `00` and `11` should occur for this circuit, with roughly 500 counts each. `trialsCount` must be positive to use `stateStats`.

```scala mdoc:invisible
assert(experiment.trials.size == 1000)
assert(experiment.trials.forall(t => Set("00", "11").contains(t.toBinary)))
```

Supply an execution context to control the simulator's parallel work. Shut down pools you create:

```scala mdoc:silent
import java.util.concurrent.ForkJoinPool
import scala.concurrent.ExecutionContext

val pool = new ForkJoinPool(2)
try {
  val context = ExecutionContext.fromExecutor(pool)
  val parallelSimulator = QuantumSimulator(context, new Random(42L))
  parallelSimulator.run(bell)
} finally {
  pool.shutdown()
}
```

The simulator applies gates sequentially and parallelizes work within a gate. Experiment trials run sequentially.

## Memory and performance

An $n$-qubit state has $2^n$ complex amplitudes. Scotty stores two four-byte floats per amplitude, so the raw state vector alone requires $8 \times 2^n$ bytes:

| Qubits | Raw state vector |
| --- | --- |
| 20 | 8 MiB |
| 25 | 256 MiB |
| 29 | 4 GiB |

The implementation's array sizes and integer indexing impose a ceiling of 29 qubits. This is not a practical capacity guarantee: heap size, parallel indices, readouts and temporary allocations add substantial overhead. Start with small circuits.

Applying a single-qubit gate scans a number of amplitudes proportional to $2^n$, so its work is $O(2^n)$ for $n$ qubits. Composite gates repeat that work for their constituent gates. Float precision also limits numerical accuracy; use tolerances when comparing probabilities.
