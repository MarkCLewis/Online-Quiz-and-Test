package onlineclassroom

import org.scalatest._
import flatspec._
import matchers._

class TestDrawingAutoGrade extends AnyFlatSpec with should.Matchers {
  "A value" should "be equal" in {
    val init = Seq(ReferenceBox(0, 0, "value"))
    val correct = Seq(ValueBox(0, 0, "42", "label"), Connector(-1, 0, 0))
    val answer = Seq(ValueBox(0, 0, "42", "label"), Connector(-1, 0, 0))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "not be equal - value" in {
    val init = Seq(ReferenceBox(0, 0, "value"))
    val correct = Seq(ValueBox(0, 0, "42", "label"), Connector(-1, 0, 0))
    val answer = Seq(ValueBox(0, 0, "45", "label"), Connector(-1, 0, 0))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "not be equal - connector" in {
    val init = Seq(ReferenceBox(0, 0, "value"))
    val correct = Seq(ValueBox(0, 0, "42", "label"), Connector(-1, 0, 0))
    val answer = Seq(ValueBox(0, 0, "42", "label"))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  "A SLL" should "be equal when identical" in {
    val init = Seq(ReferenceBox(0, 0, "head"), ReferenceBox(0, 0, "tail"))
    val correct = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2), Connector(-2, 0, 2))
    val answer = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2), Connector(-2, 0, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be equal with different order" in {
    val init = Seq(ReferenceBox(0, 0, "head"), ReferenceBox(0, 0, "tail"))
    val correct = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2), Connector(-2, 0, 2))
    val answer = Seq(DoubleBox(0, 0, "3", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "1", ""), 
      Connector(-1, 0, 2), Connector(2, 0, 1), Connector(1, 0, 0), Connector(-2, 0, 0))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be not equal wrong value" in {
    val init = Seq(ReferenceBox(0, 0, "head"), ReferenceBox(0, 0, "tail"))
    val correct = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2), Connector(-2, 0, 2))
    val answer = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "4", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2), Connector(-2, 0, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be not equal tail missing link" in {
    val init = Seq(ReferenceBox(0, 0, "head"), ReferenceBox(0, 0, "tail"))
    val correct = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2), Connector(-2, 0, 2))
    val answer = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be not equal list missing link" in {
    val init = Seq(ReferenceBox(0, 0, "head"), ReferenceBox(0, 0, "tail"))
    val correct = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2), Connector(-2, 0, 2))
    val answer = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(-2, 0, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be not equal extra link" in {
    val init = Seq(ReferenceBox(0, 0, "head"), ReferenceBox(0, 0, "tail"))
    val correct = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2), Connector(-2, 0, 2))
    val answer = Seq(DoubleBox(0, 0, "1", ""), DoubleBox(0, 0, "2", ""), DoubleBox(0, 0, "3", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(1, 0, 2), Connector(-2, 0, 2), Connector(2, 0, 0))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  "A DLL" should "be equal when identical" in {
    val init = Seq(ReferenceBox(0, 0, "sentinel"))
    val correct = Seq(TripleBox(0, 0, "", ""), TripleBox(0, 0, "1", ""), TripleBox(0, 0, "2", ""), 
      Connector(-1, 0, 0), Connector(0, 1, 1), Connector(1, 0, 0), Connector(1, 1, 2), Connector(2, 0, 1), Connector(2, 1, 0), Connector(0, 0, 2))
    val answer = Seq(TripleBox(0, 0, "", ""), TripleBox(0, 0, "1", ""), TripleBox(0, 0, "2", ""), 
      Connector(-1, 0, 0), Connector(0, 1, 1), Connector(1, 0, 0), Connector(1, 1, 2), Connector(2, 0, 1), Connector(2, 1, 0), Connector(0, 0, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be equal when different order" in {
    val init = Seq(ReferenceBox(0, 0, "sentinel"))
    val correct = Seq(TripleBox(0, 0, "", ""), TripleBox(0, 0, "1", ""), TripleBox(0, 0, "2", ""), 
      Connector(-1, 0, 0), Connector(0, 1, 1), Connector(1, 0, 0), Connector(1, 1, 2), Connector(2, 0, 1), Connector(2, 1, 0), Connector(0, 0, 2))
    val answer = Seq(TripleBox(0, 0, "", ""), TripleBox(0, 0, "2", ""), TripleBox(0, 0, "1", ""), 
      Connector(-1, 0, 0), Connector(0, 1, 2), Connector(2, 0, 0), Connector(2, 1, 1), Connector(1, 0, 2), Connector(1, 1, 0), Connector(0, 0, 1))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be not equal when a value is off" in {
    val init = Seq(ReferenceBox(0, 0, "sentinel"))
    val correct = Seq(TripleBox(0, 0, "", ""), TripleBox(0, 0, "1", ""), TripleBox(0, 0, "2", ""), 
      Connector(-1, 0, 0), Connector(0, 1, 1), Connector(1, 0, 0), Connector(1, 1, 2), Connector(2, 0, 1), Connector(2, 1, 0), Connector(0, 0, 2))
    val answer = Seq(TripleBox(0, 0, "", ""), TripleBox(0, 0, "1", ""), TripleBox(0, 0, "5", ""), 
      Connector(-1, 0, 0), Connector(0, 1, 1), Connector(1, 0, 0), Connector(1, 1, 2), Connector(2, 0, 1), Connector(2, 1, 0), Connector(0, 0, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be not equal when a link is off" in {
    val init = Seq(ReferenceBox(0, 0, "sentinel"))
    val correct = Seq(TripleBox(0, 0, "", ""), TripleBox(0, 0, "1", ""), TripleBox(0, 0, "2", ""), 
      Connector(-1, 0, 0), Connector(0, 1, 1), Connector(1, 0, 0), Connector(1, 1, 2), Connector(2, 0, 1), Connector(2, 1, 0), Connector(0, 0, 2))
    val answer = Seq(TripleBox(0, 0, "", ""), TripleBox(0, 0, "1", ""), TripleBox(0, 0, "2", ""), 
      Connector(-1, 0, 0), Connector(0, 1, 1), Connector(1, 0, 0), Connector(1, 1, 2), Connector(2, 0, 0), Connector(2, 1, 0), Connector(0, 0, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  "A BST" should "be equal when identical" in {
    val init = Seq(ReferenceBox(0, 0, "root"))
    val correct = Seq(TripleBox(0, 0, "5", ""), TripleBox(0, 0, "3", ""), TripleBox(0, 0, "7", ""), TripleBox(0, 0, "2", ""), TripleBox(0, 0, "4", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(1, 1, 4))
    val answer = Seq(TripleBox(0, 0, "5", ""), TripleBox(0, 0, "3", ""), TripleBox(0, 0, "7", ""), TripleBox(0, 0, "2", ""), TripleBox(0, 0, "4", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(1, 1, 4))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be not equal when values are different" in {
    val init = Seq(ReferenceBox(0, 0, "root"))
    val correct = Seq(TripleBox(0, 0, "5", ""), TripleBox(0, 0, "3", ""), TripleBox(0, 0, "7", ""), TripleBox(0, 0, "2", ""), TripleBox(0, 0, "4", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(1, 1, 4))
    val answer = Seq(TripleBox(0, 0, "5", ""), TripleBox(0, 0, "3", ""), TripleBox(0, 0, "7", ""), TripleBox(0, 0, "1", ""), TripleBox(0, 0, "4", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(1, 1, 4))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be not equal when links are different" in {
    val init = Seq(ReferenceBox(0, 0, "root"))
    val correct = Seq(TripleBox(0, 0, "5", ""), TripleBox(0, 0, "3", ""), TripleBox(0, 0, "7", ""), TripleBox(0, 0, "2", ""), TripleBox(0, 0, "4", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(1, 1, 4))
    val answer = Seq(TripleBox(0, 0, "5", ""), TripleBox(0, 0, "3", ""), TripleBox(0, 0, "7", ""), TripleBox(0, 0, "2", ""), TripleBox(0, 0, "4", ""), 
      Connector(-1, 0, 0), Connector(0, 1, 1), Connector(0, 0, 2), Connector(1, 0, 3), Connector(1, 1, 4))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  "An array" should "be equal when identical" in {
    val init = Seq(ReferenceBox(0, 0, "arr"))
    val correct = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), Connector(-1, 0, 0))
    val answer = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), Connector(-1, 0, 0))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be not equal when answer is missing value" in {
    val init = Seq(ReferenceBox(0, 0, "arr"))
    val correct = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), Connector(-1, 0, 0))
    val answer = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3"), ""), Connector(-1, 0, 0))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be not equal when answer has extra values" in {
    val init = Seq(ReferenceBox(0, 0, "arr"))
    val correct = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), Connector(-1, 0, 0))
    val answer = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4", "5", "6"), ""), Connector(-1, 0, 0))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be equal when identical with links" in {
    val init = Seq(ReferenceBox(0, 0, "arr"))
    val correct = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), ValueBox(0, 0, "42", ""), ValueBox(0, 0, "43", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2))
    val answer = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), ValueBox(0, 0, "42", ""), ValueBox(0, 0, "43", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be not equal when links go to different values" in {
    val init = Seq(ReferenceBox(0, 0, "arr"))
    val correct = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), ValueBox(0, 0, "42", ""), ValueBox(0, 0, "43", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2))
    val answer = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), ValueBox(0, 0, "42", ""), ValueBox(0, 0, "45", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be not equal when links are from different places" in {
    val init = Seq(ReferenceBox(0, 0, "arr"))
    val correct = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), ValueBox(0, 0, "42", ""), ValueBox(0, 0, "43", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2))
    val answer = Seq(ArrayOfBoxes(0, 0, Seq("1", "2", "3", "4"), ""), ValueBox(0, 0, "42", ""), ValueBox(0, 0, "43", ""), 
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 2, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  "A graph" should "be equal when identical from a reference" in {
    val init = Seq(ReferenceBox(0, 0, "start"))
    val correct = Seq(GraphNode(0, 0, "1"), GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(2, 0, 3))
    val answer = Seq(GraphNode(0, 0, "1"), GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(2, 0, 3))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be equal when identical from a reference with different order links" in {
    val init = Seq(ReferenceBox(0, 0, "start"))
    val correct = Seq(GraphNode(0, 0, "1"), GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(2, 0, 3))
    val answer = Seq(GraphNode(0, 0, "1"), GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(0, 1, 1), Connector(0, 0, 2), Connector(1, 0, 3), Connector(2, 0, 3))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be equal when identical from a node" in {
    val init = Seq(GraphNode(0, 0, "1"))
    val correct = Seq(GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(-1, 1, 1), Connector(0, 0, 2), Connector(1, 0, 2))
    val answer = Seq(GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(-1, 1, 1), Connector(0, 0, 2), Connector(1, 0, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (true)
  }

  it should "be not equal when different values from a reference" in {
    val init = Seq(ReferenceBox(0, 0, "start"))
    val correct = Seq(GraphNode(0, 0, "1"), GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(2, 0, 3))
    val answer = Seq(GraphNode(0, 0, "1"), GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "5"),
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 1, 2), Connector(1, 0, 3), Connector(2, 0, 3))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be not equal when different values from a node" in {
    val init = Seq(GraphNode(0, 0, "1"))
    val correct = Seq(GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(-1, 1, 1), Connector(0, 0, 2), Connector(1, 0, 2))
    val answer = Seq(GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "5"),
      Connector(-1, 0, 0), Connector(-1, 1, 1), Connector(0, 0, 2), Connector(1, 0, 2))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }

  it should "be not equal when extra edge from a reference" in {
    val init = Seq(ReferenceBox(0, 0, "start"))
    val correct = Seq(GraphNode(0, 0, "1"), GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 0, 2), Connector(1, 0, 3), Connector(2, 0, 3))
    val answer = Seq(GraphNode(0, 0, "1"), GraphNode(0, 0, "2"), GraphNode(0, 0, "3"), GraphNode(0, 0, "4"),
      Connector(-1, 0, 0), Connector(0, 0, 1), Connector(0, 0, 2), Connector(0, 0, 3), Connector(1, 0, 3), Connector(2, 0, 3))
    DrawAnswerElement.checkEquals(init, correct, answer) should be (false)
  }
}