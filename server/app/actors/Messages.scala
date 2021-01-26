package actors

import onlineclassroom.SaveAnswerInfo
import onlineclassroom.ProblemSpec
import akka.actor.ActorRef
import onlineclassroom.ProblemAnswer

object Messages {
  case class RunTest(sai: SaveAnswerInfo, testFunc: () => Boolean, makeAnswer: Boolean => ProblemAnswer)
  case class TestResult(pass: Boolean, sai: SaveAnswerInfo, makeAnswer: Boolean => ProblemAnswer)
}