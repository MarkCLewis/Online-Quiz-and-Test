package models

import scala.concurrent.Future
import onlineclassroom._

trait OCModel {
  def validateUser(username: String, password: String): Future[Option[(Int, Boolean)]]
  def createUser(username: String, password: String, instructor: Boolean): Future[Int]
  def changePassword(userid: Int, oldPassword: String, newPassword: String): Future[Boolean]
  def coursesForUser(userid: Int): Future[Seq[CourseData]]
  def addUserToCourse(userid: Int, courseid: Int): Future[Boolean]
  def addCourse(ncd:NewCourseData, userid:Int):Future[Boolean]
  def courseAssessmentData(courseid: Int): Future[CourseGradeInformation]
  def studentGradeData(userid: Int, courseid: Int): Future[FullStudentData]
  def courseStudentGradeData(courseid: Int, instructorid: Int): Future[Seq[FullStudentData]]
  def saveOrCreateProblem(spec: ProblemSpec): Future[Int]
  def allProblems(): Future[Seq[ProblemSpec]]
  def saveOrCreateAssessment(ad: AssessmentData): Future[Int]
  def allAssessments(): Future[Seq[AssessmentData]]
  def associatedProblems(assessmentid: Int): Future[Seq[ProblemAssessmentAssociation]]
  def saveProblemAssessmentAssoc(paa: ProblemAssessmentAssociation): Future[Int]
  def removeProblemAssessmentAssoc(paaid: Int): Future[Int]
  def saveAssessmentCourseAssoc(aci: AssessmentCourseInfo): Future[Int]
}