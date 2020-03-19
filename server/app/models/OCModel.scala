package models

import scala.concurrent.Future
import onlineclassroom._

trait OCModel {
  def validateUser(username: String, password: String): Future[Option[Int]]
  def createUser(username: String, password: String): Future[Option[Int]]
  def changePassword(userid: Int, oldPassword: String, newPassword: String): Future[Boolean]
  def coursesForUser(userid: Int): Future[Seq[CourseInfo]]
  def createCourse(name: String, semester: String, section: Int): Future[Option[Int]]
  def addUserToCourse(userid: Int, courseid: Int): Future[Boolean]
  def addCourse(ncd:NewCourseData, userid:Int):Future[Boolean]
}