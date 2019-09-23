package scotty.simulator

import java.util

import scotty.quantum.QuantumContext._
import scotty.quantum.gate.Gate.GateGen
import scotty.quantum.gate.StandardGate.{CPHASE00, CPHASE01, ISWAP, PSWAP}
import scotty.quantum.gate._
import scotty.quantum.math.{Complex, MathUtils}
import scotty.quantum.{Superposition, _}
import scotty.simulator.math.{MatrixWrapper, VectorWrapper}

import scala.collection.parallel.{ExecutionContextTaskSupport, ParIterable}
import scala.collection.parallel.immutable.ParVector
import scala.collection.parallel.mutable.ParArray
import scala.concurrent.ExecutionContext
import scala.util.Random

case class QuantumSimulator(ec: Option[ExecutionContext], random: Random) extends QuantumContext {
  val taskSupport: Option[ExecutionContextTaskSupport] = ec.map(new ExecutionContextTaskSupport(_))

  def measure(register: QubitRegister, state: Vector): Collapsed = {
    val initialIterator = (0, 0d, None: Option[Int])
    val rnd = random.nextDouble()

    val result = (0 until state.length / 2).foldLeft(initialIterator)((iterator, stateIndex) => {
      val abs = Complex.abs(state(2 * stateIndex), state(2 * stateIndex + 1))
      val totalProb = iterator._2 + Math.pow(abs, 2)

      val tryCollapse = (c: Int) => if (rnd <= totalProb) Some(c) else None

      iterator match {
        case (count, _, None) => (count + 1, totalProb, tryCollapse(count))
        case (count, _, valueOp) => (count + 1, totalProb, valueOp)
      }
    })

    Collapsed(register, result._3.get)
  }

  def run(circuit: Circuit): State = {
    val state = registerToState(circuit.register)
    val shouldMeasure = circuit.flattenedOps.exists(_.isInstanceOf[Measure])
    val parIndices: ParArray[Int] = ParArray.iterate(0, state.length / 4)(i => i + 1)

    taskSupport.foreach(parIndices.tasksupport = _)

    circuit.gates.foreach {
      case swap: SwapGate => ???
      case g: CPHASE00 => ???
      case g: CPHASE01 => ???
      case control: ControlGate => applyControlGate(parIndices, state, control)
      case dagger: Dagger => ???
      case target: TargetGate => applyTargetGate(parIndices, state, target)
    }

    if (shouldMeasure) measure(circuit.register, state)
    else Superposition(circuit.register, state)
  }

  def applyTargetGate(iterator: ParIterable[Int], state: Vector, gate: TargetGate): Unit = {
    val matrix = gate.matrix(this)
    val targetIndex = gate.index

    iterator.foreach(i => {
      val clearedBit = nthCleared(i, targetIndex)
      val target0Index = 2 * clearedBit
      val target1Index = 2 * (clearedBit | (1 << targetIndex))

      val zeroState = (state(target0Index), state(target0Index + 1))
      val oneState = (state(target1Index), state(target1Index + 1))

      val newZeroState = Complex.sum(
        Complex.product(matrix(0)(0), matrix(0)(1), zeroState._1, zeroState._2),
        Complex.product(matrix(0)(2), matrix(0)(3), oneState._1, oneState._2)
      )

      val newOneState = Complex.sum(
        Complex.product(matrix(1)(0), matrix(1)(1), zeroState._1, zeroState._2),
        Complex.product(matrix(1)(2), matrix(1)(3), oneState._1, oneState._2)
      )

      state(target0Index) = newZeroState._1
      state(target0Index + 1) = newZeroState._2

      state(target1Index) = newOneState._1
      state(target1Index + 1) = newOneState._2
    })
  }

  def applyControlGate(iterator: ParIterable[Int], state: Vector, control: ControlGate): Unit = {
    val matrix = control.finalTarget.matrix(this)
    val targetIndex = control.finalTarget match {
      case gate: TargetGate => gate.index
      case _ => ???
    }

    iterator.foreach(i => {
      val clearedBit = nthCleared(i, targetIndex)
      val target0Index = 2 * clearedBit
      val target1Index = 2 * (clearedBit | (1 << targetIndex))

      val zeroState = (state(target0Index), state(target0Index + 1))
      val oneState = (state(target1Index), state(target1Index + 1))

      val zeroControlsTrigger = control.controlIndexes.forall(idx => ((1 << idx) & (target0Index / 2)) > 0)
      val oneControlsTrigger = control.controlIndexes.forall(idx => ((1 << idx) & (target1Index / 2)) > 0)

      if (zeroControlsTrigger) {
        val newZeroState = Complex.sum(
          Complex.product(matrix(0)(0), matrix(0)(1), zeroState._1, zeroState._2),
          Complex.product(matrix(0)(2), matrix(0)(3), oneState._1, oneState._2)
        )

        state(target0Index) = newZeroState._1
        state(target0Index + 1) = newZeroState._2
      }

      if (oneControlsTrigger) {
        val newOneState = Complex.sum(
          Complex.product(matrix(1)(0), matrix(1)(1), zeroState._1, zeroState._2),
          Complex.product(matrix(1)(2), matrix(1)(3), oneState._1, oneState._2)
        )

        state(target1Index) = newOneState._1
        state(target1Index + 1) = newOneState._2
      }
    })
  }

  def nthCleared(n: Int, target: Int): Int = {
    val mask = (1 << target) - 1

    (n & mask) | ((n & ~mask) << 1)
  }

  def runAndMeasure(circuit: Circuit,
                    trialsCount: Int): ExperimentResult = {
    val experiments = ParVector.fill(trialsCount)(0)

    taskSupport.foreach(experiments.tasksupport = _)

    ExperimentResult(experiments.map(_ => super.runAndMeasure(circuit)).toList)
  }

  def registerToState(register: QubitRegister): Vector = {
    if (register.values.isEmpty) Array()
    else if (register.values.forall(_ == Qubit.zero)) {
      val state = Array.fill(2 * Math.pow(2, register.size).toInt)(0f)
      state(0) = 1f
      state
    }
    else if (register.values.forall(_ == Qubit.one)) {
      val state = Array.fill(2 * Math.pow(2, register.size).toInt)(0f)
      state(state.length - 1) = 1f
      state
    } else {
      register.values
        .map(q => Array(q.a.r, q.a.i, q.b.r, q.b.i))
        .reduceLeft((state, q) => VectorWrapper.tensorProduct(state, q, taskSupport))
    }
  }

  def tensorProduct(register: QubitRegister, sp1: Superposition, sp2: Superposition): Superposition =
    Superposition(register, VectorWrapper.tensorProduct(sp1.vector, sp2.vector, taskSupport))

  def product(register: QubitRegister, gate: Gate, sp: Superposition): Superposition =
    Superposition(register, MatrixWrapper.product(gate.matrix(this), sp.vector))

  def densityMatrix(vector: Vector): Matrix = VectorWrapper.ketBraOuterProduct(vector)

  def isUnitary(g: Gate): Boolean = MatrixWrapper.isUnitary(g.matrix(this))

  def gateMatrix(gate: Gate): Matrix = gate match {
    case swap: SwapGate => swapMatrix(swap)
    case g: CPHASE00 => cphase0Matrix(g, g.phi, Zero())
    case g: CPHASE01 => cphase0Matrix(g, g.phi, One())
    case dagger: Dagger => MatrixWrapper.conjugateTranspose(dagger.target.matrix(this))
    case target: TargetGate => target.customMatrix.getOrElse(targetMatrix(target))
  }

  def cphase0Matrix(gate: ControlGate, phi: Double, targetBit: Bit): Matrix = {
    val minIndex = gate.indices.min
    val controlIndex = gate.controlIndex - minIndex
    val targetIndex = gate.targetIndexes(0) - minIndex

    val qubitCount = totalQubitCount(gate)

    val finalMatrix = MatrixWrapper.identity(Math.pow(2, qubitCount).toInt)

    for (i <- finalMatrix.indices) {
      val binaries = MathUtils.toPaddedBinary(i, qubitCount).toArray

      if (binaries(controlIndex).isInstanceOf[Zero] && binaries(targetIndex) == targetBit) {
        val c = Complex.e(phi)
        finalMatrix(i)(2 * i) = c.r
        finalMatrix(i)(2 * i + 1) = c.i
      }
    }

    finalMatrix
  }

  def totalQubitCount(gate: Gate): Int = {
    val sortedControlIndexes = gate.indices.sorted

    val gapQubitCount = (sortedControlIndexes.tail, sortedControlIndexes).zipped.map((a, b) => a - b - 1).sum

    gate.qubitCount + gapQubitCount
  }

  def targetMatrix(targetGate: Gate): Matrix =
    QuantumSimulator.singleQubitGateGens(targetGate.name).apply(targetGate.params)

  def swapMatrix(gate: SwapGate): Matrix = {
    val equal = (a: Vector, b: Vector) => util.Arrays.equals(a, b)
    val notEqual = (a: Vector, b: Vector) => !equal(a, b)

    def phase(s: Vector) = {
      if (equal(s, One.floatValue)) gate match {
        case _: ISWAP => Array(Complex(0), Complex(0, 1)).toFloat
        case g: PSWAP => Array(Complex(0), Complex(Math.cos(g.phi), Math.sin(g.phi))).toFloat
        case _ => s
      } else s
    }

    val minIndex = gate.indices.min
    val i1 = gate.index1 - minIndex
    val i2 = gate.index2 - minIndex

    val qubitCount = gate.qubitCount + Math.abs(i1 - i2) - 1

    val result = (0 until Math.pow(2, qubitCount).toInt).map(stateIndex => {
      val binaries = MathUtils.toPaddedBinary(stateIndex, qubitCount).map(_.toFloatArray).toArray
      val s1 = binaries(i1)
      val s2 = binaries(i2)

      if (notEqual(s1, s2) || notEqual(s1, Zero.floatValue) && notEqual(s2, One.floatValue)) {
        val i1Val = phase(s1)

        binaries(i1) = phase(s2)
        binaries(i2) = i1Val
      }

      binaries.reduce((s1, s2) => VectorWrapper.tensorProduct(s1, s2, taskSupport))
    }).toArray

    result
  }
}

object QuantumSimulator {
  import scotty.simulator.gate._

  val singleQubitGateGens: Map[String, GateGen] = Map(
    "H" -> H.matrix,
    "X" -> X.matrix,
    "Y" -> Y.matrix,
    "Z" -> Z.matrix,
    "I" -> I.matrix,
    "S" -> S.matrix,
    "T" -> T.matrix,
    "PHASE" -> PHASE.matrix,
    "PHASE0" -> PHASE0.matrix,
    "RX" -> RX.matrix,
    "RY" -> RY.matrix,
    "RZ" -> RZ.matrix
  )

  def apply(): QuantumSimulator = QuantumSimulator(None, new Random())

  def apply(random: Random): QuantumSimulator = QuantumSimulator(None, random)

  def apply(ec: ExecutionContext): QuantumSimulator = QuantumSimulator(Some(ec), new Random)

  def apply(ec: ExecutionContext, random: Random): QuantumSimulator = QuantumSimulator(Some(ec), random)
}