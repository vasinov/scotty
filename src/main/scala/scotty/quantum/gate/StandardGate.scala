package scotty.quantum.gate

object StandardGate {
  // Single qubit gates

  case class H(index: Int) extends TargetGate

  case class I(index: Int) extends TargetGate

  case class X(index: Int) extends TargetGate

  case class Y(index: Int) extends TargetGate

  case class Z(index: Int) extends TargetGate

  case class S(index: Int) extends TargetGate

  case class T(index: Int) extends TargetGate

  case class PHASE(phi: Double, index: Int) extends TargetGate {
    override val params: Seq[Double] = Seq(phi)
  }

  case class PHASE0(phi: Double, index: Int) extends TargetGate {
    override val params: Seq[Double] = Seq(phi)
  }

  case class RX(theta: Double, index: Int) extends TargetGate {
    override val params: Seq[Double] = Seq(theta)
  }

  case class RY(theta: Double, index: Int) extends TargetGate {
    override val params: Seq[Double] = Seq(theta)
  }

  case class RZ(theta: Double, index: Int) extends TargetGate {
    override val params: Seq[Double] = Seq(theta)
  }

  // Multi qubit gates

  case class CNOT(controlIndex: Int, targetIndex: Int) extends ControlGate {
    lazy val target = X(targetIndex)
  }

  case class CCNOT(controlIndex: Int, controlIndex2: Int, targetIndex: Int) extends ControlGate {
    lazy val target = Controlled(controlIndex2, X(targetIndex))
  }

  case class CZ(controlIndex: Int, targetIndex: Int) extends ControlGate {
    lazy val target = Z(targetIndex)
  }

  case class SWAP(index1: Int, index2: Int) extends SwapGate

  case class CSWAP(controlIndex: Int, index1: Int, index2: Int) extends ControlGate {
    lazy val target = SWAP(index1, index2)
  }

  case class CPHASE(phi: Double, controlIndex: Int, targetIndex: Int) extends ControlGate {
    lazy val target = PHASE(phi, targetIndex)
  }

  case class CPHASE10(phi: Double, controlIndex: Int, targetIndex: Int) extends ControlGate {
    lazy val target = PHASE0(phi, targetIndex)
  }
}
