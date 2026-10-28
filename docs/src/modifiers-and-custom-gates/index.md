# Modifiers and Custom Gates

## Gate modifiers

`Controlled(controlIndex, target)` adds a control to a single-qubit target, or to a chain of controlled single-qubit targets. `Dagger(target)` takes the conjugate transpose of a `TargetGate` matrix.

```scala mdoc:silent
import scotty.quantum._
import scotty.quantum.gate._
import scotty.quantum.gate.StandardGate._
import scotty.quantum.math.Complex
import scotty.quantum.math.Complex._
import scotty.simulator.QuantumSimulator

implicit val simulator: QuantumContext = QuantumSimulator()
val controlledAdjoint = Controlled(0, Dagger(Y(1)))
val multipleControls = Controlled(2, Controlled(0, X(1)))
simulator.run(Circuit(controlledAdjoint, multipleControls))
```

All indices must be distinct. Control and target indices need not be adjacent or ascending. Do not wrap swap or composite gates in `Controlled`; use `CSWAP` for a controlled swap. `Dagger` accepts a single-qubit `TargetGate`, rather than a whole circuit.

## Define a single-qubit matrix

`DefGate` accepts a complex 2×2 matrix packed into two rows of four floats. The `Complex._` import provides `.toFloat` to pack an array of complex numbers.

```scala mdoc:silent
val sqrtNotMatrix = Array(
  Array(Complex(0.5, 0.5), Complex(0.5, -0.5)),
  Array(Complex(0.5, -0.5), Complex(0.5, 0.5))
).toFloat

def SqrtNot(index: Int): DefGate = DefGate(sqrtNotMatrix, index)
assert(simulator.isUnitary(SqrtNot(0)))
val twice = simulator.runAndMeasure(Circuit(SqrtNot(0), SqrtNot(0)))
assert(twice.toBinary == "1")
```

A custom quantum gate must be unitary. Use `isUnitary` to check its matrix; `DefGate` construction does not enforce this automatically.

## Parameterized gates

`TargetGate.MatrixGen` is `Seq[Double] => QuantumContext.Matrix`. You can pass one `Double` parameter directly, or a `Seq[Double]` for multiple parameters.

```scala mdoc:silent
val quarterPhaseMatrix: TargetGate.MatrixGen = (params: Seq[Double]) => Array(
  Array(Complex(1), Complex(0)),
  Array(Complex(0), Complex.e(params(0)))
).toFloat

def QuarterPhase(phi: Double, index: Int): DefGate =
  DefGate(quarterPhaseMatrix, phi / 4, index)

val customPhase = QuarterPhase(Math.PI, 0)
assert(simulator.isUnitary(customPhase))
```

## Composite gates

`CompositeGate` groups a sequence of gates. For example, a swap can be decomposed into three CNOTs:

```scala mdoc:silent
def MySwap(i0: Int, i1: Int): CompositeGate =
  CompositeGate(CNOT(i0, i1), CNOT(i1, i0), CNOT(i0, i1))

val swapped = simulator.runAndMeasure(Circuit(MySwap(0, 1)).withRegister("01"))
assert(swapped.toBinary == "10")
```

Composite gates describe unitary gate sequences. Use ordinary circuit operations for measurement.
