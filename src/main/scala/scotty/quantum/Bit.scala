package scotty.quantum

import scotty.{ErrorMessage, Labeled}
import scotty.quantum.math.Complex

sealed trait Bit extends Labeled[String] {
  def toBasisState: Array[Complex] = this match {
    case _: One => Array(Complex(0), Complex(1))
    case _: Zero => Array(Complex(1), Complex(0))
  }

  def toInt: Int = this match {
    case _: One => 1
    case _: Zero => 0
  }

  def withLabel(label: String): Bit = this match {
    case _: One => One(label)
    case _: Zero => Zero(label)
  }

  override def toString: String = this match {
    case _: One => label.fold("1")(l => s"One($l)")
    case _: Zero => label.fold("0")(l => s"Zero($l)")
  }
}

case class Zero(label: Option[String]) extends Bit
case class One(label: Option[String]) extends Bit

object Bit {
  def fromInt(value: Int): Bit = value match {
    case 0 => Zero(None)
    case 1 => One(None)
    case _ => throw new IllegalArgumentException(ErrorMessage.IntToBit)
  }
}

object One {
  def apply(): One = One(None)
  def apply(label: String): One = One(Some(label))
}

object Zero {
  def apply(): Zero = Zero(None)
  def apply(label: String): Zero = Zero(Some(label))
}