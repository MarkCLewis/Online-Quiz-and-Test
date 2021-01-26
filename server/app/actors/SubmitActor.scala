package actors

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import play.api.libs.json._
import java.sql.Savepoint
import onlineclassroom.SaveAnswerInfo

import onlineclassroom.ReadsAndWrites._
import onlineclassroom._
import models.OCModel
import models.CodeRunner
import akka.actor.PoisonPill

class SubmitActor(out: ActorRef, model: OCModel, codeRunner: ActorRef) extends Actor {
  implicit val ex = context.dispatcher
  def receive = {
    case jsVal: JsValue =>
      println("Receive " + jsVal)
      out ! Json.toJson(CodeSubmitResponse("Submission received.", false))
      Json.fromJson[SaveAnswerInfo](jsVal) match {
        case JsSuccess(sai, path) =>
          println("Parsed " + sai)
          sai.answer match {
            case WriteFunctionAnswer(code, _) => 
              testCode(sai, 
                ps => CodeRunner.checkFunction(ps.info.asInstanceOf[WriteFunctionInfo], ps.answerInfo.asInstanceOf[WriteFunctionGradeInfo], code),
                pass => WriteFunctionAnswer(code, pass))
            case WriteExpressionAnswer(code, _) => 
              testCode(sai, 
                ps => CodeRunner.checkExpression(ps.info.asInstanceOf[WriteExpressionInfo], ps.answerInfo.asInstanceOf[WriteExpressionGradeInfo], code),
                pass => WriteExpressionAnswer(code, pass))
            case WriteLambdaAnswer(code, _) => 
              testCode(sai, 
                ps => CodeRunner.checkLambda(ps.info.asInstanceOf[WriteLambdaInfo], ps.answerInfo.asInstanceOf[WriteLambdaGradeInfo], code),
                pass => WriteLambdaAnswer(code, pass))
            case _ =>
              out ! Json.toJson(CodeSubmitResponse("Unknown coding problem type.", false))
              self ! PoisonPill
          }
        case e @ JsError(_) => 
          out ! Json.toJson(CodeSubmitResponse("Error parsing submit message.", false))
          self ! PoisonPill
      }
    case Messages.TestResult(pass, sai, makeAnswer) =>
      if (pass) {
        println("Passed test.")
        out ! Json.toJson(CodeSubmitResponse("Correct.", true))
        model.setGradeData(GradeData(-1, sai.userid, sai.courseid, sai.paaid, 100.0, "Auto-graded at correct."))
        model.addAnswer(sai.copy(answer = makeAnswer(true)))
      } else {
        println("Failed test.")
        out ! Json.toJson(CodeSubmitResponse("Test failed.", false))
        model.addAnswer(sai.copy(answer = makeAnswer(false)))
      }
      self ! PoisonPill
    case m => println("Submit got: " + m)
  }

  def testCode(sai: SaveAnswerInfo, runTest: ProblemSpec => Boolean, makeAnswer: Boolean => ProblemAnswer): Unit = {
    model.oneProblemFromPAAID(sai.paaid).map {
      case Some(ps) =>
        out ! Json.toJson(CodeSubmitResponse("Testing code.", false))
        codeRunner ! Messages.RunTest(sai, () => runTest(ps), makeAnswer)
      case None =>
        out ! Json.toJson(CodeSubmitResponse("Error reading problem for tesing.", false))
        self ! PoisonPill
    }
  }
}

object SubmitActor {
  def props(out: ActorRef, model: OCModel, codeRunner: ActorRef) = Props(new SubmitActor(out, model, codeRunner))
}