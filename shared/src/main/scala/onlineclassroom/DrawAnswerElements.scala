package onlineclassroom

sealed trait DrawAnswerElement
sealed trait ConnectFromElement extends DrawAnswerElement
sealed trait ConnectToElement extends DrawAnswerElement
case class ReferenceBox(px: Double, py: Double, label: String) extends ConnectFromElement
case class ValueBox(px: Double, py: Double, value: String, label: String) extends ConnectToElement
case class DoubleBox(px: Double, py: Double, value: String, label: String) extends ConnectToElement with ConnectFromElement
case class TripleBox(px: Double, py: Double, value: String, label: String) extends ConnectToElement with ConnectFromElement
case class ArrayOfBoxes(px: Double, py: Double, values: Seq[String], label: String) extends ConnectToElement with ConnectFromElement
case class Connector(e1: Int, sub1: Int, e2: Int) extends DrawAnswerElement
case class Curve(pnts: Seq[(Double, Double)]) extends DrawAnswerElement
case class Text(px: Double, py: Double, msg: String) extends DrawAnswerElement
