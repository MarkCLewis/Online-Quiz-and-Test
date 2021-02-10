package models

import java.io.File
import java.io.PrintWriter
import sys.process._
import onlineclassroom._


object CodeRunner {
  def nestTest(testCode: String, numRuns: Int): String = {
    s"""import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
    
Future {
   Thread.sleep(10000)
   sys.exit(2)
}
for(i <- 1 to $numRuns) {
  $testCode
}
"""
  }

  def runCode(code: String, input: String, numRuns: Int): Boolean = {
    val tmpFile = File.createTempFile("test", ".scala")
    tmpFile.deleteOnExit()
    val pw = new PrintWriter(tmpFile)
    val nestedCode = nestTest(code, numRuns)
    println(nestedCode)
    pw.println(nestedCode)
    pw.close
    val process = s"${ScalaSetup.scalaHome}scala -J-Djava.security.manager -J-Djava.security.policy=mypolicy ${tmpFile.getAbsolutePath()}".run() 
    val ret = process.exitValue() == 0
    println("Done running - " + ret)
    println("Exit value is " + process.exitValue())
    ret
  }

  def checkFunction(info: WriteFunctionInfo, gradeInfo: WriteFunctionGradeInfo, submission: String): Boolean = {
    val code = s"""
      ${gradeInfo.correctCode.replaceAll(info.functionName+"\\(", info.functionName + "Correct(")}
      $submission
      ${info.varSpecs.map(_.codeGenerator()).mkString("\n")}
      val theirFunc = ${info.functionName}(${info.varSpecs.map(_.name).mkString(",")})
      val correctFunc = ${info.functionName}Correct(${info.varSpecs.map(_.name).mkString(",")})
      if(theirFunc != correctFunc) sys.exit(1)
      """
    CodeRunner.runCode(code, "", gradeInfo.numRuns)
  }

  def checkLambda(info: WriteLambdaInfo, gradeInfo: WriteLambdaGradeInfo, submission: String): Boolean = {
    val funcType = s"(${info.varSpecs.map(_.typeName).mkString(", ")}) => ${info.returnType}"
    val args = info.varSpecs.map(_.name).mkString(", ")
    val code = s"""
      def tester(f1:$funcType, f2:$funcType):Unit = {
        ${info.varSpecs.map(_.codeGenerator()).mkString("\n")}
        if(f1($args) != f2($args)) sys.exit(1)
      }
      tester($submission,${gradeInfo.correctCode})
      """
    CodeRunner.runCode(code, "", gradeInfo.numRuns)
  }

  def checkExpression(info: WriteExpressionInfo, gradeInfo: WriteExpressionGradeInfo, submission: String): Boolean = {
    val code = s"""
      ${info.varSpecs.map(_.codeGenerator()).mkString("\n")}
      ${info.generalSetup}
      if({$submission} != {${gradeInfo.correctCode}}) sys.exit(1)
      """
    CodeRunner.runCode(code, "", gradeInfo.numRuns)
  }
}

