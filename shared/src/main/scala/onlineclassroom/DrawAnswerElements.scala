package onlineclassroom

sealed trait DrawAnswerElement
case class ReferenceBox(px: Double, py: Double, label: String) extends DrawAnswerElement
case class ValueBox(px: Double, py: Double, value: String, label: String) extends DrawAnswerElement
case class DoubleBox(px: Double, py: Double, value: String, label: String) extends DrawAnswerElement
case class TripleBox(px: Double, py: Double, value: String, label: String) extends DrawAnswerElement
case class ArrayOfBoxes(px: Double, py: Double, values: Seq[String], label: String) extends DrawAnswerElement
case class GraphNode(px: Double, py: Double, label: String) extends DrawAnswerElement
case class Connector(e1: Int, sub1: Int, e2: Int) extends DrawAnswerElement
case class Curve(pnts: Seq[(Double, Double)]) extends DrawAnswerElement
case class Text(px: Double, py: Double, msg: String) extends DrawAnswerElement
