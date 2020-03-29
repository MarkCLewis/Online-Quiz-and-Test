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
  def getCourseAssessments(courseid: Int): Future[Seq[AssessmentCourseInfo]]
  def getStudentStarts(userid: Int, courseid: Int): Future[Seq[StudentAssessmentStart]]
  def getAssessmentStarts(aciid: Int): Future[Seq[StudentAssessmentStart]]
  def getAssessmentProblems(userid: Int, courseid: Int, assessmentid: Int, aciid: Int): Future[Seq[StudentProblemSpec]]
  def startAssessment(userid: Int, aciid: Int): Future[StudentAssessmentStart]
  def mergeAnswer(sai: SaveAnswerInfo): Future[Int]
  def addAnswer(sai: SaveAnswerInfo): Future[Int]
  def addStudentToCourse(email: String, courseid: Int): Future[Int]
  def assessmentGradingData(courseid: Int, assessmentid: Int): Future[AssessmentGradingData]
  def setGradeData(gd: GradeData): Future[Int]
  def updateTimeMultiplier(userid: Int, courseid: Int, newMult: Double): Future[Int]
  def getTimeMultipler(userid: Int, courseid: Int): Future[Double]
  def getFormulas(courseid: Int): Future[Seq[GradeFormulaInfo]]
  def studentAssessmentGradingData(userid: Int, courseid: Int, assessmentid: Int): Future[AssessmentGradingData]
}