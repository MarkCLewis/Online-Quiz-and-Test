package controllers

import javax.inject._

import play.api.mvc._
import play.api.libs.json._
import akka.actor.Actor
import play.api.libs.streams.ActorFlow
import akka.actor.ActorSystem
import akka.stream.Materializer

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcCapabilities
import slick.jdbc.PostgresProfile.api._
import models.OCDatabaseModel
import scala.concurrent.ExecutionContext

import onlineclassroom._
import onlineclassroom.ReadsAndWrites._
import scala.concurrent.Future
import java.sql.Timestamp

@Singleton
class Application @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)(implicit ec: ExecutionContext) 
    extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {

  import ControlHelpers._
  implicit val actionBuilder = Action

  val model = new OCDatabaseModel(db)
  model.initializeIfNeeded()

  def index = Action { implicit request =>
    Ok(views.html.index()) 
  }

  /**
    * This method is removes the boilerplate that is associated with pulling data out of a post request.
    *
    * @param f The function that is executed with the body to build the response.
    * @param request The request containing the body.
    * @param reads An implicit converter from Json.
    * @return A future of a result that is generated by the function or a redirect to the initial page.
    */
  def withJsonBody[A](f: A => Future[Result])(implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
    request.body.asJson.map { body =>
      Json.fromJson[A](body) match {
        case JsSuccess(a, path) => f(a)
        case e @ JsError(_) => 
          println(s"Error parsing received Json.\n  $body\n  $e")
          Future(Ok(Json.toJson("Error parsing message.")))
      }
    }.getOrElse(Future(Redirect(routes.Application.index())))
  }

  def tryLogin = Action.async { implicit request =>
    withJsonBody[LoginData] { li =>
      model.validateUser(li.username, li.password).flatMap { case ret => 
        ret match {
          case None =>
            Future(Ok(Json.toJson(UserData(li.username, -1, false))))
          case Some((n, instructor)) =>
            Future(Ok(Json.toJson(UserData(li.username, n, instructor)))
              .withSession(request.session + ("username" -> li.username) + ("userid" -> n.toString) + ("instructor" -> instructor.toString)))
        }
      }
    }
  }

  def logout = Action { implicit request =>
    Ok("").withSession(request.session - "username" - "userid" - "instructor")
  }

  def changePassword = AuthenticatedAction { implicit request =>
    withJsonBody[PasswordChangeData] { pcd =>
      model.changePassword(pcd.userid, pcd.oldPassword, pcd.newPassword).map(b => Ok(Json.toJson(b)))
    }
  }

  def createUser = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[NewUserData] { nud =>
      model.createUser(nud.username, nud.password, nud.instructor).map(i => Ok(Json.toJson(i > 0)))
    }
  }

  def createCourse = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[NewCourseData] { ncd =>
      model.addCourse(ncd, request.session("userid").toInt).map(b => Ok(Json.toJson(b)))
    }
  }

  def getCourses = AuthenticatedAction { implicit request =>
    withJsonBody[Int] { userid =>
      model.coursesForUser(userid).map(courses => Ok(Json.toJson(courses)))
    }
  }

  def getInstructorCourseData = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[Int] { courseid =>
      val assessmentData = model.courseAssessmentData(courseid)
      val studentData = model.courseStudentGradeData(courseid, request.session("userid").toInt)
      (for {
        ad <- assessmentData
        sd <- studentData
      } yield FullInstructorCourseData(sd, ad)).map { t => Ok(Json.toJson(t)) }
    }
  }

  def saveProblem = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[ProblemSpec] { problemSpec =>
      model.saveOrCreateProblem(problemSpec).map(id => Ok(Json.toJson(id)))
    }
  }

  def getProblems = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[Int] { userid =>
      model.allProblems().map(probs => Ok(Json.toJson(probs)))
    }
  }

  def saveAssessment = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[AssessmentData] { ad =>
      model.saveOrCreateAssessment(ad).map(id => Ok(Json.toJson(id)))
    }
  }

  def getAssessments = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[Int] { userid =>
      model.allAssessments().map(ads => Ok(Json.toJson(ads)))
    }
  }

  def loadAssociatedProblems = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[Int] { assessmntid =>
      model.associatedProblems(assessmntid).map(paas => Ok(Json.toJson(paas)))
    }
  }

  def saveProblemAssessmentAssoc = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[ProblemAssessmentAssociation] { paa =>
      model.saveProblemAssessmentAssoc(paa).map(id => Ok(Json.toJson(id)))
    }
  }

  def removeProblemAssessmentAssoc = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[Int] { paaid =>
      model.removeProblemAssessmentAssoc(paaid).map(cnt => Ok(Json.toJson(cnt)))
    }
  }

  def saveAssessmentCourseAssoc = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[AssessmentCourseInfo] { aci =>
      println(aci)
      model.saveAssessmentCourseAssoc(aci).map(id => Ok(Json.toJson(id)))
    }
  }

  def getServerTime = AuthenticatedAction { implicit request =>
    withJsonBody[Int] { userid =>
      Future.successful(Ok(Json.toJson(new Timestamp(System.currentTimeMillis()).toString())))
    }
  }

  def getCourseAssessments = AuthenticatedAction { implicit request =>
    withJsonBody[(Int, Int)] { case (userid, courseid) =>
      model.getCourseAssessments(courseid).map(as => Ok(Json.toJson(as)))
    }
  }

  def getStudentStarts = AuthenticatedAction { implicit request =>
    withJsonBody[(Int, Int)] { case (userid, courseid) =>
      model.getStudentStarts(userid, courseid).map(starts => Ok(Json.toJson(starts)))
    }
  }

  def getAssessmentStarts = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[Int] { aciid =>
      model.getAssessmentStarts(aciid).map(starts => Ok(Json.toJson(starts)))
    }
  }

  def getAssessmentProblems = AuthenticatedAction { implicit request =>
    withJsonBody[(Int, Int, Int, Int)] { case (userid, courseid, assessmentid, aciid) =>
      model.getAssessmentProblems(userid, courseid, assessmentid, aciid).map(as => Ok(Json.toJson(as)))
    }
  }

  def startAssessment = AuthenticatedAction { implicit request =>
    withJsonBody[(Int, Int)] { case (userid, aciid) =>
      model.startAssessment(userid, aciid).map(time => Ok(Json.toJson(time)))
    }
  }

  def mergeAnswer = AuthenticatedAction { implicit request =>
    withJsonBody[SaveAnswerInfo] { sai =>
      model.mergeAnswer(sai).map(aid => Ok(Json.toJson(aid)))
    }
  }

  def addAnswer = AuthenticatedAction { implicit request =>
    // TODO: This doesn't currently handle auto-grading. That will be needed.
    withJsonBody[SaveAnswerInfo] { sai =>
      model.mergeAnswer(sai).map(aid => Ok(Json.toJson(aid)))
    }
  }

  def addStudentToCourse = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[(String, Int)] { case (email, courseid) =>
      model.addStudentToCourse(email, courseid).map(num => Ok(Json.toJson(num)))
    }
  }

  def assessmentGradingData = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[(Int, Int)] { case (courseid, assessmentid) =>
      model.assessmentGradingData(courseid, assessmentid).map(agd => Ok(Json.toJson(agd)))
    }
  }

  def setGradeData = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[GradeData] { gd =>
      model.setGradeData(gd).map(newid => Ok(Json.toJson(newid)))
    }
  }

  def updateTimeMultiplier = AuthenticatedInstructorAction { implicit request =>
    withJsonBody[(Int, Int, Double)] { case (userid, courseid, newMult) =>
      model.updateTimeMultiplier(userid, courseid, newMult).map(num => Ok(Json.toJson(num)))
    }
  }

  def getTimeMultipler = AuthenticatedAction { implicit request =>
    withJsonBody[(Int, Int)] { case (userid, courseid) => 
      model.getTimeMultipler(userid, courseid).map(mult => Ok(Json.toJson(mult)))
    }
  }
}
