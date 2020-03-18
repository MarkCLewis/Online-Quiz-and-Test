package models

import scala.concurrent.Future
import onlineclassroom._

/**
  * This is a model that will be held in memory. The goal of this is basically to work as a stub for testing. I want
  * to use the same interface as I will in the database version so this will return futures instead of plain values.
  * If all is done well it should be possible to simply swap what is instantiated in the Controller.
  */
class OCMemoryModel {
  def validateUser(username: String, password: String): Future[Option[Int]] = ???

  def createUser(username: String, password: String): Future[Option[Int]] = ???

  def coursesForUser(userid: Int): Future[Seq[CourseInfo]] = ???

  
}