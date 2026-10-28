# Standard Gates

Import standard gates from `scotty.quantum.gate.StandardGate._`. Angles are in **radians**. Constructors put angle parameters before numeric qubit indices.

```scala mdoc:silent
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator

val circuit = Circuit(H(0), RX(Math.PI / 2, 1), CNOT(0, 1))
val state = QuantumSimulator().run(circuit)
```

## Single-qubit gates

| Constructor | Effect |
| --- | --- |
| `H(index)` | Hadamard: creates an equal superposition from a basis state. |
| `I(index)` | Identity. |
| `X(index)` | Pauli X: exchanges zero and one amplitudes. |
| `Y(index)` | Pauli Y: exchanges amplitudes with factors $-i$ and $i$. |
| `Z(index)` | Pauli Z: negates the one amplitude. |
| `RX(theta, index)` | Rotation about the x-axis by $\theta$. |
| `RY(theta, index)` | Rotation about the y-axis by $\theta$. |
| `RZ(theta, index)` | Rotation about the z-axis by $\theta$. |
| `PHASE(phi, index)` | Multiplies the one amplitude by $e^{i\phi}$. |
| `PHASE0(phi, index)` | Multiplies the zero amplitude by $e^{i\phi}$. |
| `S(index)` | `PHASE` with $\phi=\pi/2$. |
| `T(index)` | `PHASE` with $\phi=\pi/4$. |

## Controlled and swap gates

| Constructor | Qubits | Effect |
| --- | --- | --- |
| `CNOT(control, target)` | 2 | Applies X when the control is one. |
| `CCNOT(control1, control2, target)` | 3 | Applies X when both controls are one (Toffoli). |
| `CZ(control, target)` | 2 | Applies Z when the control is one. |
| `SWAP(index1, index2)` | 2 | Exchanges two qubits. |
| `CSWAP(control, index1, index2)` | 3 | Exchanges two qubits when the control is one (Fredkin). |
| `ISWAP(index1, index2)` | 2 | Exchanges `01` and `10`, multiplying their amplitudes by $i$. |
| `PSWAP(phi, index1, index2)` | 2 | Exchanges `01` and `10`, multiplying their amplitudes by $e^{i\phi}$. |
| `CPHASE(phi, control, target)` | 2 | Applies a phase to control=1, target=1. |
| `CPHASE00(phi, control, target)` | 2 | Applies a phase to control=0, target=0. |
| `CPHASE10(phi, control, target)` | 2 | Applies a phase to control=1, target=0. |
| `CPHASE01(phi, control, target)` | 2 | Applies a phase to control=0, target=1. |

For the phase variants, suffixes describe **control then target**, not necessarily the left-to-right order of a displayed register string. Gate indices use the [ordering described here](../circuits-and-qubits/index.md#register-ordering). Qubit indices within a controlled or swap gate must be distinct.

## Swap phase caveat in 0.6.0

The simulator's `ISWAP` and `PSWAP` decompositions also apply an extra phase to `11`: `ISWAP` multiplies it by $-i$, and `PSWAP(phi, ...)` multiplies it by $e^{3i\phi}$. This differs from the conventional definitions, which leave `11` unchanged. The table describes the exchange of `01` and `10`; do not assume these gates preserve the phase of a general two-qubit state. Use an explicit, verified decomposition when that phase matters.

```scala mdoc:invisible
implicit val context: QuantumContext = QuantumSimulator()
val iswap11 = StateProbabilityReader(context.run(
  Circuit(ISWAP(0, 1)).withRegister("11"))).read.head
assert(scala.math.abs(iswap11.amplitude.r) < 0.00001)
assert(scala.math.abs(iswap11.amplitude.i + 1.0) < 0.00001)
val phi = Math.PI / 7
val pswap11 = StateProbabilityReader(context.run(
  Circuit(PSWAP(phi, 0, 1)).withRegister("11"))).read.head
assert(scala.math.abs(pswap11.amplitude.r - Math.cos(3 * phi)) < 0.00001)
assert(scala.math.abs(pswap11.amplitude.i - Math.sin(3 * phi)) < 0.00001)
```

## Implementation

`TargetGate` supplies a single-qubit matrix. `ControlGate` supplies a control index and a target gate. `SwapGate` supplies two indices. The simulator implements swap and some phase gates as sequences of simpler gates.

Each gate step has work proportional to the number of state amplitudes: $O(2^n)$ for $n$ qubits. A fixed-size composite gate repeats a fixed number of those steps. Gate steps run sequentially, with parallel work inside each step.

See [Modifiers and Custom Gates](../modifiers-and-custom-gates/index.md) for controlled, adjoint and custom operations.
