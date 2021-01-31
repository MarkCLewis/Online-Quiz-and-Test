package onlineclassroom

import scala.util.matching.Regex

object FormulaParser {
  val binOpMap: Map[Char, (Double, Double) => Double] = Map(
    '+' -> (_ + _),
    '-' -> (_ - _),
    '*' -> (_ * _),
    '/' -> (_ / _)
  )

  val unaryOpMap: Map[Char, Double => Double] = Map(
    '-' -> (-_),
    '+' -> (+_)
  )

  val functionMap: Map[String, Seq[Double] => Double] = Map(
    "avg" -> (xs => if (xs.isEmpty) 0.0 else xs.sum/xs.length),
    "avgDrop1" -> (xs => if (xs.length < 2) 0.0 else (xs.sum - xs.min) / (xs.length - 1)),
    "avgDrop2" -> (xs => if (xs.length < 3) 0.0 else xs.sorted.drop(2).sum / (xs.length - 2)),
    "sum" -> (xs => xs.sum),
    "count" -> (xs => xs.length)
  )

  def apply(formula: String): Option[FNode] = if (formula.trim.isEmpty) None else parse(formula)

  def parse(expr: String): Option[FNode] = {
    val f = expr.trim
    var opLoc = -1
    var pcnt = 0
    var i = f.length - 1
    while (i > 0) {
      f(i) match {
        case ')' => pcnt += 1
        case '(' => pcnt -= 1
        case '+'|'-' if pcnt == 0 && i < f.length-1 => 
          opLoc = i
          i = -1
        case '*'|'/' if pcnt == 0 && i < f.length-1 && opLoc < 0 =>
          opLoc = i
        case _ =>
      }
      i -= 1
    }
    if (opLoc < 0) {
      if (f.head == '(' && f.last == ')') parse(f.substring(1, f.length - 1))
      else if (unaryOpMap.contains(f.head)) {
        try {
          Some(ConstNode(f.toDouble)) // This isn't required, but it is more efficient for evaluation.
        } catch {
          case ex: NumberFormatException => parse(f.tail).map(n => UnaryOpNode(n, unaryOpMap(f.head)))
        }
      }
      else if (f.contains('(') && f.last == ')') {
        val openIndex = f.indexOf('(')
        val funcName = f.substring(0, openIndex)
        val args = f.substring(openIndex+1, f.length()-1)
        if (args.contains(',')) {
          val argBuilder = new StringBuilder
          var pcnt = 0
          var argList = List[Option[FNode]]()
          for (c <- args) c match {
            case '(' => pcnt += 1
            case ')' => pcnt -= 1
            case ',' if pcnt == 0 =>
              argList ::= parse(argBuilder.toString())
              argBuilder.clear()
            case _ => argBuilder.append(c)
          }
          if (argBuilder.nonEmpty) argList ::= parse(argBuilder.toString())
          if (argList.forall(_.nonEmpty)) functionMap.get(funcName).map(f => (FunctionNode(argList.reverse.flatten, f))) else None
        } else {
          functionMap.get(funcName).flatMap(f => try { Some(FunctionOnRegexNode(args.r, f)) } catch { case ex: Exception => None})
        }
      } else try {
        Some(ConstNode(f.toDouble))
      } catch {
        case ex: NumberFormatException => Some(GradeNode(f))
      }
    } else {
      val oleft = parse(f.substring(0, opLoc))
      val oright = parse(f.substring(opLoc + 1))
      oleft.flatMap(left => oright.map(right => BinaryOpNode(left, right, binOpMap(f(opLoc)))))
    }
  }

  sealed trait FNode {
    def eval(grades: Map[String, Double]): Option[Double]
  }

  case class ConstNode(x: Double) extends FNode {
    def eval(grades: Map[String, Double]): Option[Double] = Some(x)
  }

  case class GradeNode(gradeName: String) extends FNode {
    def eval(grades: Map[String, Double]): Option[Double] = grades.get(gradeName)
  }

  case class BinaryOpNode(left: FNode, right: FNode, op: (Double, Double) => Double) extends FNode {
    def eval(grades: Map[String, Double]): Option[Double] = {
      for (l <- left.eval(grades); r <- right.eval(grades)) yield op(l, r)
    }
  }

  case class UnaryOpNode(arg: FNode, op: (Double) => Double) extends FNode {
    def eval(grades: Map[String, Double]): Option[Double] = arg.eval(grades).map(op)
  }

  case class FunctionNode(args: Seq[FNode], func: Seq[Double] => Double) extends FNode {
    def eval(grades: Map[String, Double]): Option[Double] = args.foldLeft(Option(List[Double]())){ (acc, arg) => 
      for (lst <- acc; x <- arg.eval(grades)) yield x :: lst
    }.map(xs => func(xs.reverse))
  }

  case class FunctionOnRegexNode(regex: Regex, func: Seq[Double] => Double) extends FNode {
    def eval(grades: Map[String, Double]): Option[Double] = Some(func((for ((k, v) <- grades; if regex.matches(k)) yield v).toSeq))
  }
}