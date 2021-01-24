package onlineclassroom

object VariableType extends Enumeration {
  type Type = Value
  val Int, Double, String = Value
}

sealed trait VariableSpec {
  val name: String
  val typeName: String
  def codeGenerator(): String // Return a string that is code to generate this value. 
}

case class IntSpec(name: String, min: Int, max: Int) extends VariableSpec {
  val typeName = "Int"

  def codeGenerator(): String = {
    s"val $name = util.Random.nextInt(($max)-($min))+($min)"
  }
}

case class DoubleSpec(name: String, min: Double, max: Double) extends VariableSpec {
  val typeName = "Double"

  def codeGenerator(): String = {
    s"val $name = math.random*(($max)-($min))+($min)"
  }
}

case class StringSpec(name: String, length: Int, genCode: String) extends VariableSpec {
  val typeName = "String"

  def codeGenerator(): String = {
    if(genCode.isEmpty())
      s"val $name = (for(i <- 0 until $length) yield { ('a'+util.Random.nextInt(26)).toChar }).mkString"
    else
      s"val $name = $genCode"
  }
}

case class ListIntSpec(name: String, minLen:Int, maxLen:Int, min: Int, max: Int) extends VariableSpec {
  val typeName = "List[Int]"

  def codeGenerator(): String = {
    s"val $name = List.fill(util.Random.nextInt(($maxLen)-($minLen))+($minLen))(util.Random.nextInt(($max)-($min))+($min))"
  }
}

case class ArrayIntSpec(name: String, minLen:Int, maxLen:Int, min: Int, max: Int) extends VariableSpec {
  val typeName = "Array[Int]"

  def codeGenerator(): String = {
    s"val $name = Array.fill(util.Random.nextInt(($maxLen)-($minLen))+($minLen))(util.Random.nextInt(($max)-($min))+($min))"
  }
}

case class ListStringSpec(name: String, minLen:Int, maxLen:Int, stringLength: Int, genCode: String) extends VariableSpec {
  val typeName = "List[String]"

  def codeGenerator(): String = {
    if(genCode.isEmpty())
      s"val $name = List.fill(util.Random.nextInt(($maxLen)-($minLen))+($minLen)){(for(i <- 0 until $stringLength) yield { ('a'+util.Random.nextInt(26)).toChar }).mkString}"
    else
      s"val $name = List.fill(util.Random.nextInt(($maxLen)-($minLen))+($minLen)){$genCode}"
  }
}

case class ArrayArrayIntSpec(name: String, minLen1:Int, maxLen1:Int, minLen2:Int, maxLen2:Int, min: Int, max: Int) extends VariableSpec {
  val typeName = "Array[Array[Int]]"

  def codeGenerator(): String = {
    s"val $name = Array.fill(util.Random.nextInt(($maxLen1)-($minLen1))+($minLen1),"+
      s"util.Random.nextInt(($maxLen2)-($minLen2))+($minLen2))(util.Random.nextInt(($max)-($min))+($min))"
  }
}

case class ArrayArrayDoubleSpec(name: String, minLen1:Int, maxLen1:Int, minLen2:Int, maxLen2:Int, min: Double, max: Double) extends VariableSpec {
  val typeName = "Array[Array[Double]]"

  def codeGenerator(): String = {
    s"val $name = Array.fill(util.Random.nextInt(($maxLen1)-($minLen1))+($minLen1),"+
      s"util.Random.nextInt(($maxLen2)-($minLen2))+($minLen2))(math.random*(($max)-($min))+($min))"
  }
}

