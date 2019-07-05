package scotty.simulator

import scotty.quantum._
import scotty.quantum.QuantumContext._
import scotty.quantum.StandardGate
import scotty.quantum.math.MathUtils
import scotty.simulator.math.Implicits._
import scala.util.Random
import scotty.quantum.math.Complex
import scotty.simulator.QuantumSimulator.GateGen
import scotty.simulator.math.linearalgebra.Types.ApacheVector
import scotty.simulator.math.linearalgebra.{MatrixWrapper, VectorWrapper}

case class QuantumSimulator()(implicit random: Random = new Random) extends QuantumContext {
  val gateGenerators: Map[String, GateGen] = QuantumSimulator.standardGates

  def measure(register: QubitRegister, sp: Superposition): Collapsed = {
    val initialIterator = (0, 0d, None: Option[Int])
    val result = sp.probabilities.foldLeft(initialIterator)((iterator, prob) => {
      val probSum = iterator._2 + prob
      val tryCollapse = (c: Int) => if (prob > 0 && random.nextDouble() <= probSum) Some(c) else None

      iterator match {
        case (count, _, None) => (count + 1, probSum, tryCollapse(count))
        case (count, _, valueOp) => (count + 1, probSum, valueOp)
      }
    })._3

    Collapsed(register, result.get)
  }

  def run(circuit: Circuit): State = {
    val shouldMeasure = circuit.ops.exists(op => op.isInstanceOf[Measure])

    val result = circuit.ops
      .flatMap(opToGate(_, circuit.register.size))
      .foldLeft(registerToSuperposition(circuit.register))((state, gate) => state.applyGate(gate)(this))

    if (shouldMeasure) measure(circuit.register, result) else result
  }

  def registerToSuperposition(register: QubitRegister): Superposition =
    register.values.foldLeft(Superposition())((superposition, q) =>
      superposition.combine(Superposition(q))(this))

  def opToGate(op: Op, qubitCount: Int): collection.Seq[Gate] = op match {
    case c: CircuitConnector => c.circuit.ops.flatMap(o => opToGate(o, qubitCount))
    case g: Gate => Seq(prepareGate(g, qubitCount))
    case m: Measure => Seq(prepareGate(StandardGate.I(m.index), qubitCount))
  }

  def prepareGate(gate: Gate, qubitCount: Int): Gate = {
    def pad(): Seq[Gate] = {
      val identityGate = RawGate(Array(
        Array(Complex(1), Complex(0)),
        Array(Complex(0), Complex(1))
      ))

      def topPad = (0 until gate.indexes.sortWith(_ < _)(0)).map(_ => identityGate)
      def bottomPad = (gate.indexes.sortWith(_ > _)(0) until qubitCount - 1).map(_ => identityGate)

      (topPad :+ gate) ++ bottomPad
    }

    pad().reduce((a, b) => a.par(b)(this))
  }

  def tensorProduct(g1: Gate, g2: Gate): Gate = RawGate(
    (MatrixWrapper(g1.matrix(this)) ⊗ MatrixWrapper.fieldMatrix(g2.matrix(this))).getData
  )

  def tensorProduct(sp1: Superposition, sp2: Superposition): Superposition = Superposition(
    (VectorWrapper(sp1.vector) ⊗ VectorWrapper.fieldVector(sp2.vector)).getData
  )

  def product(gate: Gate, sp: Superposition): Superposition = Superposition(
    (MatrixWrapper(gate.matrix(this)) * VectorWrapper.fieldVector(sp.vector)).getData
  )

  def isUnitary(g: Gate): Boolean = MatrixWrapper(g.matrix(this)).isUnitaryMatrix

  /**
    * Generates a matrix based on the top-level control gate and nested control and target child gates.
    *
    * First, it generates an array of arrays. Each array is a binary representation of the decimal state vector index.
    * For example, a vector of length 2 can be represented with the following matrix:
    *
    * Array(
    *   Array(0, 0), Array(0, 1),
    *   Array(1, 0), Array(1, 1)
    * )
    *
    * Second, for each top-level array we check if control bits are triggered and if they are then we apply the final
    * target gate to the target qubits.
    *
    * @param gate control gate that this method generates a matrix for
    * @return final matrix representing the control gate acting on all involved qubits
    */
  def controlMatrix(gate: Control): Matrix = {
    val minIndex = gate.indexes.min
    val normalizedControlIndexes = gate.controlIndexes.map(i => i - minIndex)
    val sortedControlIndexes = gate.indexes.sorted
    val gapQubitCount = (sortedControlIndexes.tail, sortedControlIndexes).zipped.map((a, b) => a - b - 1).sum
    val qubitCount = gate.qubitCount + gapQubitCount
    val normalizedTargetIndexes = gate.targetIndexes.map(_ - minIndex)
    val stateCount = Math.pow(2, qubitCount).toInt
    val finalMatrix = Array.ofDim[Vector](stateCount)

    for (i <- 0 until stateCount) {
      val binaries = MathUtils.toBinaryPadded(i, qubitCount).toArray

      val allControlsTrigger = binaries.zipWithIndex.forall(b => {
        if (normalizedControlIndexes.contains(b._2))
          if (b._1 == One()) true else false
        else true
      })

      finalMatrix(i) = if (allControlsTrigger) {
        val ntis = normalizedTargetIndexes
        val filledNtis = if (ntis.length > 1) ntis(0) to ntis.last else ntis

        val targetRegister = QubitRegister(filledNtis.map(i => Qubit(binaries(i).toBasisState)))

        val gateTargetProduct = MatrixWrapper(gate.finalTarget.matrix(this)) *
          VectorWrapper.fieldVector(registerToSuperposition(targetRegister).vector)

        type LabeledVector = (ApacheVector, Option[String])

        binaries
          .zipWithIndex
          .map {
            case (_, index) if filledNtis.contains(index) => gateTargetProduct -> Some("target")
            case (binary, _) => VectorWrapper.fieldVector(binary.toBasisState) -> None
          }
          .foldLeft(Seq[LabeledVector]()) {
            case (acc, item) if item._2.contains("target") && acc.exists(_._2.contains("target")) => acc
            case (acc, item) => acc :+ item
          }
          .map(_._1)
          .reduce((s1, s2) => VectorWrapper(s1.getData).tensorProduct(s2)).getData
      } else {
        binaries.map(b => Superposition(b.toBasisState)).reduce((s1, s2) => s1.combine(s2)(this)).vector
      }
    }

    finalMatrix
  }

  def targetMatrix(targetGate: Gate): Matrix = gateGenerators(targetGate.name).apply(targetGate.params)

  def swapMatrix(gate: QubitSwap): Matrix = {
    val minIndex = gate.indexes.min
    val i1 = gate.index1 - minIndex
    val i2 = gate.index2 - minIndex

    val qubitCount = gate.qubitCount + Math.abs(i1 - i2) - 1

    (0 until Math.pow(2, qubitCount).toInt).map(index => {
      val binaries = MathUtils.toBinaryPadded(index, qubitCount).map(_.toBasisState).toArray
      val i1Val = binaries(i1)

      binaries(i1) = binaries(i2)
      binaries(i2) = i1Val

      binaries.map(b => Superposition(b)).reduce((s1, s2) => s1.combine(s2)(this)).vector
    }).toArray
  }
}

object QuantumSimulator {
  import scotty.simulator.gate._

  type GateGen = Seq[Double] => Matrix

  def standardGates: Map[String, GateGen] = Map(
    "H" -> H.matrix,
    "X" -> X.matrix,
    "Y" -> Y.matrix,
    "Z" -> Z.matrix,
    "I" -> I.matrix,
    "RX" -> RX.matrix,
    "RY" -> RY.matrix,
    "RZ" -> RZ.matrix
  )
}