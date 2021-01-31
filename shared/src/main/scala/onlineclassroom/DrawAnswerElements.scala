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

object DrawAnswerElement {
  def checkEquals(initialElements: Seq[DrawAnswerElement], correctElements: Seq[DrawAnswerElement], answerElements: Seq[DrawAnswerElement]): Boolean = {
    val correctMap = initialElements.zipWithIndex.map(t => -1-t._2 -> t._1).toMap ++ correctElements.zipWithIndex.map(t => t._2 -> t._1).toMap
    val answerMap = initialElements.zipWithIndex.map(t => -1-t._2 -> t._1).toMap ++ answerElements.zipWithIndex.map(t => t._2 -> t._1).toMap
    val correctEdges = correctElements.collect { case c: Connector => c }.groupBy(_.e1).map(t => t._1 -> t._2.sortBy(_.sub1))
    val ansEdges = answerElements.collect { case c: Connector => c }.groupBy(_.e1).map(t => t._1 -> t._2.sortBy(_.sub1))
    def recur(ce: Int, ae: Int, visited: Set[Int]): Boolean = {
      val newVisited = visited + ce
      (correctMap.contains(ce) && answerMap.contains(ae)) &&
      ((correctMap(ce), answerMap(ae)) match {
        case (crb: ReferenceBox, arb: ReferenceBox) => 
          crb.label == arb.label && 
          correctEdges.get(ce).map(_.length) == ansEdges.get(ae).map(_.length) && 
          (visited(ce) || !correctEdges.contains(ce) ||
          (correctEdges(ce), ansEdges(ae)).zipped.forall((nce, nae) => nce.sub1 == nae.sub1 && recur(nce.e2, nae.e2, newVisited)))
        case (cvb: ValueBox, avb: ValueBox) => cvb.value == avb.value
        case (cdb: DoubleBox, adb: DoubleBox) => 
          cdb.value == adb.value && correctEdges.get(ce).map(_.length) == ansEdges.get(ae).map(_.length) && 
          (visited(ce) || !correctEdges.contains(ce) ||
          (correctEdges(ce), ansEdges(ae)).zipped.forall((nce, nae) => nce.sub1 == nae.sub1 && recur(nce.e2, nae.e2, newVisited)))
        case (ctb: TripleBox, atb: TripleBox) =>
          ctb.value == atb.value && correctEdges.get(ce).map(_.length) == ansEdges.get(ae).map(_.length) && 
          (visited(ce) || !correctEdges.contains(ce) ||
          (correctEdges(ce), ansEdges(ae)).zipped.forall((nce, nae) => nce.sub1 == nae.sub1 && recur(nce.e2, nae.e2, newVisited)))
        case (caob: ArrayOfBoxes, aaob: ArrayOfBoxes) =>
          val cvals = caob.values.reverse.dropWhile(_.isEmpty).reverse
          val avals = aaob.values.reverse.dropWhile(_.isEmpty).reverse
          cvals.length == avals.length && (cvals, avals).zipped.forall(_ == _) && 
          correctEdges.get(ce).map(_.length) == ansEdges.get(ae).map(_.length) && 
          (visited(ce) || !correctEdges.contains(ce) ||
          (correctEdges(ce), ansEdges(ae)).zipped.forall((nce, nae) => nce.sub1 == nae.sub1 && recur(nce.e2, nae.e2, newVisited)))
        case (cgn: GraphNode, agn: GraphNode) =>
          cgn.label == agn.label && correctEdges.get(ce).map(_.length) == ansEdges.get(ae).map(_.length) && 
          (visited(ce) || !correctEdges.contains(ce) || 
          correctEdges(ce).permutations.exists { cperm => (cperm, ansEdges(ae)).zipped.forall((nce, nae) => recur(nce.e2, nae.e2, newVisited))})
        case _ => false
      })
    }
    initialElements.indices.forall { ce => recur(-ce-1, -ce-1, Set.empty) }
  }
}