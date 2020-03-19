package models

import scala.concurrent.Future
import onlineclassroom._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext
import models.Tables._
import org.mindrot.jbcrypt.BCrypt

class OCDatabaseModel(db: Database)(implicit ec: ExecutionContext) extends OCModel {
  val Student = 0
  val Instructor = 1

  def initializeIfNeeded(): Unit = {
    println("In init")
    db.run(Users.length.result).map { cnt => 
      println(cnt)
      if (cnt == 0) {
        db.run(Users += UsersRow(0,"root",Some(BCrypt.hashpw("password", BCrypt.gensalt()))))
      }
    }
  }
  
  def validateUser(username: String, password: String): Future[Option[Int]] = {
    val userRows = db.run(Users.filter(u => u.email === username).result)
    userRows.map(urow => if (urow.isEmpty) None else {
      if (BCrypt.checkpw(password, urow.head.password.getOrElse(""))) Some(urow.head.id) else None
    })
  }

  def createUser(username: String, password: String): Future[Option[Int]] = ???

  def changePassword(userid: Int, oldPassword: String, newPassword: String): Future[Boolean] = {
    val userRows = db.run(Users.filter(u => u.id === userid).result)
    userRows.flatMap(urow => if (urow.isEmpty) Future.successful(false) else {
      if (BCrypt.checkpw(oldPassword, urow.head.password.getOrElse(""))) {
        db.run(Users.filter(u => u.id === userid).update(UsersRow(urow.head.id, urow.head.email, Some(BCrypt.hashpw(newPassword, BCrypt.gensalt()))))).map(_ > 0)
      } else Future.successful(false)
    })
  }

  def coursesForUser(userid: Int): Future[Seq[CourseInfo]] = ???

  def createCourse(name: String, semester: String, section: Int): Future[Option[Int]] = ???

  def addUserToCourse(userid: Int, courseid: Int): Future[Boolean] = ???

  def addCourse(ncd:NewCourseData, userid:Int): Future[Boolean] = {
    val StudentRegex = """(\d{7})|(\w{2,8})@trinity\.edu""".r
    // Create course entry
    db.run(Course += CourseRow(0,ncd.name,ncd.semester,ncd.section)).flatMap { cnt =>
      if(cnt>0) {
        db.run(Course.filter(cr => cr.name === ncd.name && cr.semester === ncd.semester && cr.section === ncd.section).result.head).flatMap(cr => {
          for {
            // Add current user as instructor
            irec1 <- db.run(UserCourseAssoc += UserCourseAssocRow(Some(userid),Some(cr.id),Instructor))
            // Parse students and add associations
            d = (for(StudentRegex(id,uname) <- ncd.studentData.split("\n").map(_.trim)) yield {
              println("Matching "+id+", "+uname)
              (id,uname)
            }).dropWhile(_._1 == null)
            if d.length>1
            userTuples = for(((a,null),(null,d)) <- d.zip(d.tail)) yield (a,d)
            matches <- Future.sequence(for((id,uname) <- userTuples.toSeq) yield db.run(Users.filter(u => u.email === uname).result).map(rows => (id, uname, rows)))
          } yield {
            for ((tuid, uname, m) <- matches) {
                if(m.isEmpty) {
                  val hashedPwd = BCrypt.hashpw(tuid, BCrypt.gensalt())
                  db.run( Users += UsersRow(0,uname,Some(hashedPwd)) ).foreach(_ =>
                    db.run(Users.filter(u => u.email === uname).result).foreach { newUser =>
                      db.run( UserCourseAssoc += UserCourseAssocRow(Some(newUser.head.id),Some(cr.id),Student) )
                    })
                } else {
                  db.run( UserCourseAssoc += UserCourseAssocRow(Some(m.head.id),Some(cr.id),Student) )
                }
              }
            true
          }
        } )
      } else {
        println("cnt was "+cnt+" on insert of course")
        Future.successful(false)
      }
    }
  }

}