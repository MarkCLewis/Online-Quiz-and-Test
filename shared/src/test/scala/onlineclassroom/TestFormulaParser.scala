package onlineclassroom

import org.scalatest._
import flatspec._
import matchers._

class TestFormulaParser extends AnyFlatSpec with should.Matchers {
  "A simple formula" should "be 5" in {
    FormulaParser("2+3").get.eval(Map.empty) should be (Some(5))
  }

  it should "be 14" in {
    FormulaParser("2+3*4").get.eval(Map.empty) should be (Some(14))
  }

  it should "be 20" in {
    FormulaParser("(2+3)*4").get.eval(Map.empty) should be (Some(20))
  }

  it should "be 4" in {
    FormulaParser("(2+3)*4/5").get.eval(Map.empty) should be (Some(4))
  }
  
  "A formula with a grade" should "be 5" in {
    FormulaParser("2+Test").get.eval(Map("Test" -> 3.0)) should be (Some(5))
  }

  it should "be None when the variable is missing" in {
    FormulaParser("2+Quiz").get.eval(Map("Test" -> 3.0)) should be (None)
  }

  "A broken expression" should "be None when it ends in an operator" in {
    FormulaParser("2+3*").flatMap(_.eval(Map.empty)) should be (None)
  }

  it should "be None when parentheses aren't closed" in {
    FormulaParser("(2+3*4").flatMap(_.eval(Map.empty)) should be (None)
  }

  it should "be None when parentheses aren't opened" in {
    FormulaParser("2+3)*4").flatMap(_.eval(Map.empty)) should be (None)
  }

  "A formula with a function" should "work for sum of args" in {
    FormulaParser("sum(2, 3, 4)").get.eval(Map.empty) should be (Some(9))
  }

  it should "work for sum with complex args" in {
    FormulaParser("sum(2, 3, 2*3-1-1)").get.eval(Map.empty) should be (Some(9))
  }

  it should "work for a regex sum" in {
    FormulaParser("sum(Quiz \\d+)").get.eval(Map("Quiz 1" -> 1.0, "Quiz 2" -> 2.0, "Test" -> 3.0, "Quiz 90" -> 4.0)) should be (Some(7.0))
  }

  it should "work for average of args" in {
    FormulaParser("avg(1, 2, 3, 4)").get.eval(Map.empty) should be (Some(2.5))
  }

  it should "work for average of regex" in {
    FormulaParser("avg(Quiz \\d+)").get.eval(Map("Quiz 1" -> 1.0, "Quiz 2" -> 2.0, "Test" -> 3.0, "Quiz 90" -> 3.0)) should be (Some(2.0))
  }

  it should "work for average drop 1 of args" in {
    FormulaParser("avgDrop1(1, 2, 3, 4)").get.eval(Map.empty) should be (Some(3.0))
  }

  it should "work for average drop 1 of regex" in {
    FormulaParser("avgDrop1(Quiz \\d+)").get.eval(Map("Quiz 1" -> 1.0, "Quiz 2" -> 2.0, "Test" -> 3.0, "Quiz 3" -> 3.0, "Quiz 4" -> 4.0)) should be (Some(3.0))
  }
}