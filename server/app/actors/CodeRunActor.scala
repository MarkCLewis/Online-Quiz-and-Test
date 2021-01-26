package actors

import akka.actor.Actor
import models.CodeRunner
import models.OCModel

/**
  * This actor exists to make it so that all code submissions are run one at a time. This is intended to
  * prevent memory issues on Heroku that could occur running multiple scala scripts at the same time.
  */
class CodeRunActor extends Actor {
  def receive = {
    case Messages.RunTest(sai, testFunc, makeAnswer) =>
      sender ! Messages.TestResult(testFunc(), sai, makeAnswer)
    case m => println("Unhandled Message in CodeRunActor: " + m)
  }
}