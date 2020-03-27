package models

import scala.concurrent.Future
import onlineclassroom._
import onlineclassroom.ReadsAndWrites._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext
import models.Tables._
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json._
import java.sql.Timestamp

class OCDatabaseModel(db: Database)(implicit ec: ExecutionContext) extends OCModel {
  def initializeIfNeeded(): Unit = {
    println("In init")
    db.run(Users.length.result).map { cnt => 
      if (cnt == 0) {
        db.run { Users += UsersRow(0,"root", BCrypt.hashpw("password", BCrypt.gensalt()), true) }
        db.run { Users += UsersRow(0,"student", BCrypt.hashpw("password", BCrypt.gensalt()), false) }
      }
    }
  }
  
  def validateUser(username: String, password: String): Future[Option[(Int, Boolean)]] = {
    val userRows = db.run(Users.filter(u => u.email === username).result)
    userRows.map(urow => if (urow.isEmpty) None else {
      if (BCrypt.checkpw(password, urow.head.password)) Some(urow.head.id, urow.head.instructor) else None
    })
  }

  def createUser(username: String, password: String, instructor: Boolean): Future[Int] = {
    val userRows = db.run(Users.filter(u => u.email === username).result)
    userRows.flatMap { urow =>
      if (urow.nonEmpty) Future.successful(0) else {
        db.run(Users += UsersRow(0, username, BCrypt.hashpw(password, BCrypt.gensalt()), instructor))
      }
    }
  }

  def changePassword(userid: Int, oldPassword: String, newPassword: String): Future[Boolean] = {
    val userRows = db.run(Users.filter(u => u.id === userid).result)
    userRows.flatMap { urow => 
      if (urow.isEmpty) Future.successful(false) else {
        if (BCrypt.checkpw(oldPassword, urow.head.password)) {
          db.run(Users.filter(u => u.id === userid).update(urow.head.copy(password = BCrypt.hashpw(newPassword, BCrypt.gensalt())))).map(_ > 0)
        } else Future.successful(false)
      }
    }
  }

  def coursesForUser(userid: Int): Future[Seq[CourseData]] = {
    db.run(
      (for {
        (ucar,cr) <- UserCourseAssoc join Course on (_.courseid === _.id)
        if ucar.userid === userid
      } yield {
        cr
      }).result
    ).map(courseRows => for (cr <- courseRows) yield CourseData(cr.id, cr.name, cr.semester, cr.section))
  }

  def addUserToCourse(userid: Int, courseid: Int): Future[Boolean] = ???

  def addCourse(ncd:NewCourseData, userid:Int): Future[Boolean] = {
    val StudentRegex = """(\d{7})|(\w{2,8}@\w+\.edu)""".r
    // Create course entry
    db.run(Course += CourseRow(0,ncd.name,ncd.semester,ncd.section)).flatMap { cnt =>
      if(cnt>0) {
        db.run(Course.filter(cr => cr.name === ncd.name && cr.semester === ncd.semester && cr.section === ncd.section).result.head).flatMap(cr => {
          for {
            // Add current user as instructor
            irec1 <- db.run(UserCourseAssoc += UserCourseAssocRow(userid, cr.id, 1.0))
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
                  db.run( Users += UsersRow(0, uname, hashedPwd, false) ).foreach(_ =>
                    db.run(Users.filter(u => u.email === uname).result).foreach { newUser =>
                      db.run( UserCourseAssoc += UserCourseAssocRow(newUser.head.id, cr.id, 1.0) )
                    })
                } else {
                  db.run( UserCourseAssoc += UserCourseAssocRow(m.head.id, cr.id, 1.0) )
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

  def courseAssessmentData(courseid: Int): Future[CourseGradeInformation] = {
    val assessments = db.run((for {
      aca <- AssessmentCourseAssoc
      if aca.courseid === courseid
      assessment <- Assessment
      if assessment.id === aca.assessmentid
    } yield {
      (aca, assessment)
    }).result).map(as => as.map { case (aca, assessment) => AssessmentCourseInfo(aca.id, aca.courseid, assessment.id, assessment.name, assessment.description, 
      aca.points, aca.gradeGroup, aca.autoGrade, aca.startTime.map(_.toString()), aca.endTime.map(_.toString()), aca.timeLimit)})
    val formulas = db.run((for {
      f <- GradeFormula
      if f.courseid === courseid
    } yield f).result).map(formulas => formulas.map(f => GradeFormulaInfo(f.id, f.gradeGroup, f.formula)))
    for(gfi <- formulas; aci <- assessments) yield CourseGradeInformation(aci, gfi)
  }

  def calculateStudentAssessmentPercent(): Future[Double] = {
    ???
  }

  def studentAssessmentPercent(userid: Int, courseid: Int, assessmentid: Int): Future[Double] = {
    val ungrouped = db.run((for {
      paa <- ProblemAssessmentAssoc
      if paa.assessmentid === assessmentid
      (answer, grade) <- Answer.joinLeft(AnswerGrade).on(_.id === _.answerid)
      if answer.paaid === paa.id && answer.userid === userid && answer.courseid === courseid
    } yield (paa.id, paa.weight, paa.extraCredit, grade.map(_.percentCorrect))).result)
    ungrouped.map(_.groupBy(t => (t._1, t._2, t._3))
      .map { case (paaInfo, nums) => paaInfo -> nums.maxBy(_._4.getOrElse(0.0))._4})
      .map { g =>
        println(s"Calc percent $userid with $g")
        val totalWeight: Double = g.map { case ((paaid, weight, ec), percent) => if (ec) 0.0 else weight}.sum
        val totalPoints = g.map { case ((paaid, weight, ec), percent) => weight * percent.getOrElse(0.0)}.sum
        println(s"$totalWeight / $totalPoints")
        if (totalWeight == 0.0) 0.0 else totalPoints / totalWeight
      }
  }

  def studentGradeData(userid: Int, courseid: Int): Future[FullStudentData] = {
    val student = db.run(Users.filter(_.id === userid).result)
    val uca = db.run(UserCourseAssoc.filter(ucaRow => ucaRow.userid === userid && ucaRow.courseid === courseid).result)
    val assnInfo = db.run((for {
      aca <- AssessmentCourseAssoc
      if aca.courseid === courseid
      assessment <- Assessment
      if assessment.id === aca.assessmentid
    } yield (assessment.name, assessment.id, aca.points)).result)
    val grades = assnInfo.flatMap(s => Future.sequence(s.map { case (name, aid, points) => studentAssessmentPercent(userid, courseid, aid).map(p => name -> p*points/100.0)}))
    for {
      s <- student
      t <- uca
      g <- grades
    } yield {
      FullStudentData(s.head.id, s.head.email, g.toMap, t.head.timeMultiplier)
    }
  }

  def courseStudentGradeData(courseid: Int, instructorid: Int): Future[Seq[FullStudentData]] = {
    db.run(UserCourseAssoc.filter(uca => uca.courseid === courseid && uca.userid =!= instructorid).result).flatMap(s => Future.sequence(s.map(uca => studentGradeData(uca.userid, uca.courseid))))
  }

  def saveOrCreateProblem(spec: ProblemSpec): Future[Int] = {
    if (spec.id < 0) {
      db.run(Problem += ProblemRow(-1, Json.toJson(spec).toString)).flatMap(num => db.run(Problem.map(_.id).max.result)).map(_.getOrElse(-1))
    } else {
      db.run(Problem.filter(_.id === spec.id).update(ProblemRow(spec.id, Json.toJson(spec).toString))).map(_ => spec.id)
    }
  }

  def allProblems(): Future[Seq[ProblemSpec]] = {
    db.run(Problem.result).map(prs => prs.flatMap { pr => 
      Json.fromJson[ProblemSpec](Json.parse(pr.spec)) match {
        case JsSuccess(ps, path) =>
          Some(ProblemSpec(pr.id, ps.info, ps.answerInfo))
        case e@JsError(_) => 
          println("Error parsing spec for problem " + pr)
          None
      }
    })
  }

  def saveOrCreateAssessment(ad: AssessmentData): Future[Int] = {
    if(ad.id < 0) {
      db.run(Assessment += AssessmentRow(-1, ad.name, ad.description, ad.autoGrade)).flatMap(num => db.run(Assessment.map(_.id).max.result)).map(_.getOrElse(-1))
    } else {
      db.run(Assessment.filter(_.id === ad.id).update(AssessmentRow(ad.id, ad.name, ad.description, ad.autoGrade))).map(_ => ad.id)
    }
  }

  def allAssessments(): Future[Seq[AssessmentData]] = {
    db.run(Assessment.result).map(arows => arows.map(arow => AssessmentData(arow.id, arow.name, arow.description, arow.autoGrade)))
  }

  def associatedProblems(assessmentid: Int): Future[Seq[ProblemAssessmentAssociation]] = {
    db.run(ProblemAssessmentAssoc.filter(_.assessmentid === assessmentid).result).map(arows => arows.map(paaRow => ProblemAssessmentAssociation(paaRow.id, paaRow.assessmentid, paaRow.problemid, paaRow.weight, paaRow.extraCredit)))
  }

  def saveProblemAssessmentAssoc(paa: ProblemAssessmentAssociation): Future[Int] = {
    if(paa.id < 0) {
      db.run(ProblemAssessmentAssoc += ProblemAssessmentAssocRow(-1, paa.assessmentid, paa.problemid, paa.weight, paa.extraCredit)).flatMap(num => db.run(ProblemAssessmentAssoc.map(_.id).max.result)).map(_.getOrElse(-1))
    } else {
      db.run(ProblemAssessmentAssoc.filter(_.id === paa.id).update(ProblemAssessmentAssocRow(paa.id, paa.assessmentid, paa.problemid, paa.weight, paa.extraCredit))).map(_ => paa.id)
    }
  }

  def removeProblemAssessmentAssoc(paaid: Int): Future[Int] = {
    db.run(ProblemAssessmentAssoc.filter(_.id === paaid).delete)
  }

  def saveAssessmentCourseAssoc(aci: AssessmentCourseInfo): Future[Int] = {
    println(aci)
    if(aci.id < 0) {
      db.run(AssessmentCourseAssoc += AssessmentCourseAssocRow(-1, aci.courseid, aci.assessmentid, aci.points, aci.group, aci.autoGrade, aci.start.map(Timestamp.valueOf), aci.end.map(Timestamp.valueOf), aci.timeLimit)).flatMap(num => db.run(AssessmentCourseAssoc.map(_.id).max.result)).map(_.getOrElse(-1))
    } else {
      db.run(AssessmentCourseAssoc.filter(_.id === aci.id).update(AssessmentCourseAssocRow(aci.id, aci.courseid, aci.assessmentid, aci.points, aci.group, aci.autoGrade, aci.start.map(Timestamp.valueOf), aci.end.map(Timestamp.valueOf), aci.timeLimit))).map(_ => aci.id)
    }
  }

  def getCourseAssessments(courseid: Int): Future[Seq[AssessmentCourseInfo]] = {
    db.run((for {
      aca <- AssessmentCourseAssoc
      if aca.courseid === courseid
      assessment <- Assessment
      if assessment.id === aca.assessmentid
    } yield {
      (aca, assessment)
    }).result).map(as => as.map { case (aca, assessment) => AssessmentCourseInfo(aca.id, aca.courseid, assessment.id, assessment.name, assessment.description, 
      aca.points, aca.gradeGroup, aca.autoGrade, aca.startTime.map(_.toString()), aca.endTime.map(_.toString()), aca.timeLimit)})
  }

  def getStudentStarts(userid: Int, courseid: Int): Future[Seq[StudentAssessmentStart]] = {
    db.run {
      (for {
        aca <- AssessmentCourseAssoc
        if aca.courseid === courseid
        start <- AssessmentStartTime
        if start.acaid === aca.id && start.userid === userid
      } yield start).result
    }.map(_.map(start => StudentAssessmentStart(start.id, start.userid, start.acaid, start.timeStarted.toString)))
  }

  def getAssessmentStarts(aciid: Int): Future[Seq[StudentAssessmentStart]] = {
    db.run (AssessmentStartTime.filter(_.acaid === aciid).result)
      .map(_.map(astRow => StudentAssessmentStart(astRow.id, astRow.userid, astRow.acaid, astRow.timeStarted.toString)))
  }

  def getAssessmentProblems(userid: Int, courseid: Int, assessmentid: Int, aciid: Int): Future[Seq[StudentProblemSpec]] = {
    val fInfo = db.run {
      (for {
        paa <- ProblemAssessmentAssoc
        if paa.assessmentid === assessmentid
        problem <- Problem
        if problem.id === paa.problemid
      } yield (paa, problem)).result
    }
    fInfo.flatMap{info => 
      Future.sequence(info.map { case (paa, problem) =>
        val answers = db.run((for {
          answer <- Answer
          if answer.userid === userid && answer.courseid === courseid && answer.paaid === paa.id
        } yield answer).result)
        val fanswer = answers.map(_.lastOption)
        fanswer.map { answerRow =>
          val probAnswer = answerRow.map(ar => Json.fromJson[ProblemAnswer](Json.parse(ar.details)).asOpt.getOrElse(ProblemAnswerError(s"Error parsing: ${ar.details}")))
          val info = Json.fromJson[ProblemSpec](Json.parse(problem.spec)).asOpt.map(_.info).getOrElse(ProblemInfoError("Error", s"Parsing: ${problem.spec}"))
          StudentProblemSpec(paa.id, assessmentid, paa.problemid, paa.weight, paa.extraCredit, info, probAnswer)
        }
      }).map(_.sortBy(_.problemid))
    }
  }

  def startAssessment(userid: Int, acaid: Int): Future[StudentAssessmentStart] = {
    val fCurrentStarts = db.run ( AssessmentStartTime.filter(astRow => astRow.userid === userid && astRow.acaid === acaid).result )
    fCurrentStarts.flatMap(currentStarts => if (currentStarts.isEmpty) {
      val now = new Timestamp(System.currentTimeMillis())
      db.run ( AssessmentStartTime += AssessmentStartTimeRow(-1, userid, acaid, now)).map(_ => StudentAssessmentStart(-1, userid, acaid, now.toString))
    } else {
      val cs = currentStarts.head
      Future.successful(StudentAssessmentStart(cs.id, cs.userid, cs.acaid, cs.timeStarted.toString()))
    })
  }

  def mergeAnswer(sai: SaveAnswerInfo): Future[Int] = {
    if (sai.id >= 0) {
      db.run(Answer.filter(_.id === sai.id)
        .update(AnswerRow(sai.id, sai.userid, sai.courseid, sai.paaid, new Timestamp(System.currentTimeMillis()), Json.toJson(sai.answer).toString())))
        .map(cnt => sai.id)
    } else {
      val fcurrent = db.run(Answer.filter(row => row.userid === sai.userid && row.courseid === sai.courseid && row.paaid === sai.paaid).result)
      fcurrent.flatMap(current => if (current.isEmpty) addAnswer(sai) else {
        val cid = current.head.id
        db.run(Answer.filter(_.id === cid).update(AnswerRow(cid, sai.userid, sai.courseid, sai.paaid, new Timestamp(System.currentTimeMillis()), Json.toJson(sai.answer).toString())))
          .map(cnt => cid)
      })
    }
  }

  def addAnswer(sai: SaveAnswerInfo): Future[Int] = {
    db.run(Answer += AnswerRow(sai.id, sai.userid, sai.courseid, sai.paaid, new Timestamp(System.currentTimeMillis()), Json.toJson(sai.answer).toString())).
      flatMap(cnt => db.run(Answer.map(_.id).max.getOrElse(-1).result))
  }

  def addStudentToCourse(email: String, courseid: Int): Future[Int] = {
    println(s"Searching for $email")
    val student = db.run(Users.filter(_.email === email).result)
    student.flatMap { s => 
      println(s"Adding $s")
      if (s.nonEmpty) {
        db.run(UserCourseAssoc += UserCourseAssocRow(s.head.id, courseid, 1.0))
      } else Future.successful(0)}
  }

  def assessmentGradingData(courseid: Int, assessmentid: Int): Future[AssessmentGradingData] = {
    val fullResult = db.run (Assessment.filter(_.id === assessmentid).join(ProblemAssessmentAssoc).on(_.id === _.assessmentid).
      join(Problem).on(_._2.problemid === _.id).joinLeft(Answer).on((t, ans) => t._1._2.id === ans.paaid && ans.courseid === courseid).
      joinLeft(AnswerGrade).on((t, agrow) => t._2.map(_.id === agrow.answerid)).result)
    fullResult.map { seq =>
      seq.map { case ((((assessment, paa), prob), ans), ag) => (assessment, paa, prob, ans, ag) }
        .groupBy { case (assessment, paa, prob, ans, ag) => assessment }
        .map { case (assessment, aData) =>
          val problems = aData.groupBy(t => (t._2 -> t._3))
            .map { case ((paa, problem), probData) =>
              val answers = probData.flatMap { case (assessment, paa, problem, ansRowOpt, agOpt) =>
                ansRowOpt.map(ar =>
                  GradeAnswer(ar.id, ar.userid, ar.courseid, ar.paaid, ar.submitTime.toString(), 
                    Json.fromJson[ProblemAnswer](Json.parse(ar.details)).asOpt.getOrElse(ProblemAnswerError("Info error: " + ar.details)),
                    agOpt.map(agr => GradeData(agr.id, agr.answerid, agr.percentCorrect, agr.comments)))
                )
              }
              GradingProblemData(problem.id, Json.fromJson[ProblemSpec](Json.parse(problem.spec)).asOpt.getOrElse{println(s"info error with: ${problem.spec}"); ProblemSpec(-1, ProblemInfoError("Info error", problem.spec), null)}, answers)
            }.toSeq
          AssessmentGradingData(assessment.id, assessment.name, assessment.description, problems.sortBy(_.id))
        }.head
    }
  }

  def setGradeData(gd: GradeData): Future[Int] = {
    if (gd.id < 0) {
      db.run(AnswerGrade += AnswerGradeRow(-1, gd.answerid, gd.percentCorrect, gd.comments)).
        flatMap(cnt => db.run(AnswerGrade.map(_.id).max.getOrElse(-1).result))
    } else {
      db.run(AnswerGrade.filter(_.id === gd.id).update(AnswerGradeRow(gd.id, gd.answerid, gd.percentCorrect, gd.comments))).map(cnt => gd.id)
    }
  }

  def updateTimeMultiplier(userid: Int, courseid: Int, newMult: Double): Future[Int] = {
    db.run ( UserCourseAssoc.filter(ucaRow => ucaRow.userid === userid && ucaRow.courseid === courseid).map(_.timeMultiplier).update(newMult) )
  }

  def getTimeMultipler(userid: Int, courseid: Int): Future[Double] = {
    db.run ( UserCourseAssoc.filter(ucaRow => ucaRow.userid === userid && ucaRow.courseid === courseid).map(_.timeMultiplier).result ).map(_.head)
  }
}