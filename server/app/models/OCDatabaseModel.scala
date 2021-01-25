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
    ).map(courseRows => for (cr <- courseRows) yield CourseData(cr.id, cr.name, cr.semester, cr.section, cr.active))
  }

  def addUserToCourse(userid: Int, courseid: Int): Future[Boolean] = ???

  def addCourse(ncd:NewCourseData, userid:Int): Future[Boolean] = {
    val StudentRegex = """(\d{7})|(\w{2,8}@\w+\.edu)""".r
    // Create course entry
    db.run(Course += CourseRow(0,ncd.name,ncd.semester,ncd.section, true)).flatMap { cnt =>
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

  def studentAssessmentPercent(userid: Int, courseid: Int, assessmentid: Int): Future[Double] = {
    val ungrouped = db.run((for {
      (paa, grade) <- ProblemAssessmentAssoc.filter(_.assessmentid === assessmentid)
        .joinLeft(AnswerGrade.filter(ga => ga.userid === userid && ga.courseid === courseid))
        .on(_.id === _.paaid)
    } yield (paa.id, paa.weight, paa.extraCredit, grade.map(_.percentCorrect))).result)
    ungrouped.map(_.groupBy(t => (t._1, t._2, t._3))
      .map { case (paaInfo, nums) => paaInfo -> nums.maxBy(_._4.getOrElse(0.0))._4})
      .map { g =>
        val totalWeight: Double = g.map { case ((paaid, weight, ec), percent) => if (ec) 0.0 else weight}.sum
        val totalPoints = g.map { case ((paaid, weight, ec), percent) => weight * percent.getOrElse(0.0)}.sum
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

  def saveOrCreateProblem(spec: ProblemSpec, userid: Int): Future[Int] = {
    if (spec.id < 0) {
      val fMaxId = db.run(Problem.map(_.id).max.result)
      fMaxId.flatMap { maxId =>
        val id = maxId.map(_ + 1).getOrElse(0)
        db.run(Problem += ProblemRow(id, Json.toJson(spec.copy(id = id)).toString, userid)).map(num => id)
      }
    } else {
      db.run(Problem.filter(_.id === spec.id).update(ProblemRow(spec.id, Json.toJson(spec).toString, userid))).map(_ => spec.id)
    }
  }

  def oneProblemFromPAAID(paaid: Int): Future[Option[ProblemSpec]] = {
    db.run(ProblemAssessmentAssoc.filter(_.id === paaid).join(Problem).on(_.problemid === _.id).result).map(prs => prs.flatMap { case (_, pr) => 
      Json.fromJson[ProblemSpec](Json.parse(pr.spec)) match {
        case JsSuccess(ps, path) =>
          Some(ProblemSpec(pr.id, ps.info, ps.answerInfo, Some(pr.creatorid)))
        case e@JsError(_) => 
          println("Error parsing spec for problem " + pr + "\n" + e)
          None
      }
    }.headOption)
  }

  def allProblems(): Future[Seq[ProblemSpec]] = {
    db.run(Problem.result).map(prs => prs.flatMap { pr => 
      Json.fromJson[ProblemSpec](Json.parse(pr.spec)) match {
        case JsSuccess(ps, path) =>
          Some(ProblemSpec(pr.id, ps.info, ps.answerInfo, Some(pr.creatorid)))
        case e@JsError(_) => 
          println("Error parsing spec for problem " + pr + "\n" + e)
          None
      }
    })
  }

  def saveOrCreateAssessment(ad: AssessmentData, userid: Int): Future[Int] = {
    if(ad.id < 0) {
      db.run(Assessment += AssessmentRow(-1, ad.name, ad.description, ad.autoGrade, userid)).flatMap(num => db.run(Assessment.map(_.id).max.result)).map(_.getOrElse(-1))
    } else {
      db.run(Assessment.filter(_.id === ad.id).update(AssessmentRow(ad.id, ad.name, ad.description, ad.autoGrade, userid))).map(_ => ad.id)
    }
  }

  def allAssessments(): Future[Seq[AssessmentData]] = {
    db.run(Assessment.result).map(arows => arows.map(arow => AssessmentData(arow.id, arow.name, arow.description, arow.autoGrade, arow.creatorid)))
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
        val fanswer = answers.map(as => if (as.isEmpty) None else Some(as.maxBy(_.id)))
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
    val student = db.run(Users.filter(_.email === email).result)
    student.flatMap { s => 
      if (s.nonEmpty) {
        db.run(UserCourseAssoc += UserCourseAssocRow(s.head.id, courseid, 1.0))
      } else Future.successful(0)}
  }

  def getAnswers(courseid: Int, paa: ProblemAssessmentAssocRow): Future[Seq[GradeAnswer]] = {
    db.run(Answer.filter(a => a.courseid === courseid && a.paaid === paa.id).result)
      .map(_.map(ar => GradeAnswer(ar.id, ar.userid, ar.courseid, ar.paaid, ar.submitTime.toString(), 
          Json.fromJson[ProblemAnswer](Json.parse(ar.details)).asOpt.getOrElse(ProblemAnswerError("Info error: " + ar.details)))))
  }

  def getGrades(courseid: Int, paa: ProblemAssessmentAssocRow): Future[Seq[GradeData]] = {
    db.run(AnswerGrade.filter(ag => ag.courseid === courseid && ag.paaid === paa.id).result)
      .map(_.map(ag => GradeData(ag.id, ag.userid, ag.courseid, ag.paaid, ag.percentCorrect, ag.comments)))
  }

  def assessmentGradingData(courseid: Int, assessmentid: Int): Future[AssessmentGradingData] = {
    val fstudents = db.run(UserCourseAssoc.filter(_.courseid === courseid).join(Users.filter(!_.instructor)).on(_.userid === _.id).result)
      .map(_.map { case (_, userRow) => UserData(userRow.email, userRow.id, userRow.instructor)})
    val fullResult = db.run (Assessment.filter(_.id === assessmentid).join(ProblemAssessmentAssoc).on(_.id === _.assessmentid).
      join(Problem).on(_._2.problemid === _.id).result)
    fullResult.flatMap { seq => fstudents.flatMap { students =>
      seq.map { case ((assessment, paa), prob) => (assessment, paa, prob) }
        .groupBy { case (assessment, paa, prob) => assessment }
        .map { case (assessment, aData) =>
          val fproblems = Future.sequence(aData.groupBy(t => (t._2 -> t._3))
            .map { case ((paa, problem), probData) =>
              for {
                answers <- getAnswers(courseid, paa)
                grades <- getGrades(courseid, paa)
              } yield {
                GradingProblemData(problem.id, paa.id, Json.fromJson[ProblemSpec](Json.parse(problem.spec)).asOpt.getOrElse{
                  println(s"info error with: ${problem.spec}"); ProblemSpec(-1, ProblemInfoError("Info error", problem.spec), null, Some(problem.creatorid))
                }.copy(id = problem.id), answers, grades)
              }
            }.toSeq)
          fproblems.map(problems => AssessmentGradingData(assessment.id, assessment.name, assessment.description, problems.sortBy(_.id), students))
        }.head
    } }
  }

  def setGradeData(gd: GradeData): Future[Int] = {
    if (gd.id < 0) {
      val fCurrent = db.run(AnswerGrade.filter(ag => ag.userid === gd.userid && ag.courseid === gd.courseid && ag.paaid === gd.paaid).result)
      fCurrent.flatMap { current =>
        if (current.isEmpty) {
          db.run(AnswerGrade += AnswerGradeRow(-1, gd.userid, gd.courseid, gd.paaid, gd.percentCorrect, gd.comments)).
            flatMap(cnt => db.run(AnswerGrade.map(_.id).max.getOrElse(-1).result))
        } else {
          db.run(AnswerGrade.filter(_.id === current.head.id).update(AnswerGradeRow(gd.id, gd.userid, gd.courseid, gd.paaid, gd.percentCorrect, gd.comments))).map(cnt => gd.id)        }
      }
    } else {
      db.run(AnswerGrade.filter(_.id === gd.id).update(AnswerGradeRow(gd.id, gd.userid, gd.courseid, gd.paaid, gd.percentCorrect, gd.comments))).map(cnt => gd.id)
    }
  }

  def updateTimeMultiplier(userid: Int, courseid: Int, newMult: Double): Future[Int] = {
    db.run ( UserCourseAssoc.filter(ucaRow => ucaRow.userid === userid && ucaRow.courseid === courseid).map(_.timeMultiplier).update(newMult) )
  }

  def getTimeMultipler(userid: Int, courseid: Int): Future[Double] = {
    db.run ( UserCourseAssoc.filter(ucaRow => ucaRow.userid === userid && ucaRow.courseid === courseid).map(_.timeMultiplier).result ).map(_.head)
  }

  def getFormulas(courseid: Int): Future[Seq[GradeFormulaInfo]] = {
    db.run((for {
      f <- GradeFormula
      if f.courseid === courseid
    } yield f).result).map(formulas => formulas.map(f => GradeFormulaInfo(f.id, f.gradeGroup, f.formula)))
  }

  def getStudentAnswers(userid: Int, courseid: Int, paa: ProblemAssessmentAssocRow): Future[Seq[GradeAnswer]] = {
    db.run(Answer.filter(a => a.userid === userid && a.courseid === courseid && a.paaid === paa.id).result)
      .map(_.map(ar => GradeAnswer(ar.id, ar.userid, ar.courseid, ar.paaid, ar.submitTime.toString(), 
          Json.fromJson[ProblemAnswer](Json.parse(ar.details)).asOpt.getOrElse(ProblemAnswerError("Info error: " + ar.details)))))
  }

  def getStudentGrades(userid: Int, courseid: Int, paa: ProblemAssessmentAssocRow): Future[Seq[GradeData]] = {
    db.run(AnswerGrade.filter(ag => ag.userid === userid && ag.courseid === courseid && ag.paaid === paa.id).result)
      .map(_.map(ag => GradeData(ag.id, ag.userid, ag.courseid, ag.paaid, ag.percentCorrect, ag.comments)))
  }

  def studentAssessmentGradingData(userid: Int, courseid: Int, assessmentid: Int): Future[AssessmentGradingData] = { 
    val fullResult = db.run (Assessment.filter(_.id === assessmentid).join(ProblemAssessmentAssoc).on(_.id === _.assessmentid).
      join(Problem).on(_._2.problemid === _.id).result)
    fullResult.flatMap { seq =>
      seq.map { case ((assessment, paa), prob) => (assessment, paa, prob) }
        .groupBy { case (assessment, paa, prob) => assessment }
        .map { case (assessment, aData) =>
          val fproblems = Future.sequence(aData.groupBy(t => (t._2 -> t._3))
            .map { case ((paa, problem), probData) =>
              for {
                answers <- getStudentAnswers(userid,courseid, paa)
                grades <- getStudentGrades(userid,courseid, paa)
              } yield {
                GradingProblemData(problem.id, paa.id, Json.fromJson[ProblemSpec](Json.parse(problem.spec)).asOpt.getOrElse{
                  println(s"info error with: ${problem.spec}"); ProblemSpec(-1, ProblemInfoError("Info error", problem.spec), null, Some(problem.creatorid))
                }.copy(id = problem.id), answers, grades)
              }
            }.toSeq)
          fproblems.map(problems => AssessmentGradingData(assessment.id, assessment.name, assessment.description, problems.sortBy(_.id), Nil))
        }.head
    }
  }

  def getInstructors(): Future[Seq[UserData]] = {
    db.run(Users.filter(_.instructor).result).map(_.map(userRow => UserData(userRow.email, userRow.id, userRow.instructor)))
  }

  def autoGrade(agr: AutoGradeRequest): Future[AutoGradeResponse] = {
    val fProblems = db.run (
      (for {
        paa <- ProblemAssessmentAssoc
        if paa.assessmentid === agr.assessmentid
        problem <- Problem
        if problem.id === paa.problemid
      } yield {
        (paa, problem)
      }).result
    )
    fProblems.map { problemData =>
      for ((paa, prob) <- problemData) yield {
        Json.fromJson[ProblemSpec](Json.parse(prob.spec)) match {
          case JsSuccess(spec, path) =>
            (spec.info, spec.answerInfo) match {
              case (mci: MultipleChoiceInfo, mcgi: MultipleChoiceGradeInfo) =>
                val fAnswers = db.run (Answer.filter(ar => ar.courseid === agr.courseid && ar.paaid === paa.id).result)
                fAnswers.foreach { allAnswers =>
                  for ((_, answers) <- allAnswers.groupBy(a => (a.userid, a.courseid, a.paaid))) checkMultipleChoice(answers.maxBy(_.id), mcgi)
                }
              case (di: DrawingInfo, dgi: DrawingGradeInfo) =>
                val fAnswers = db.run (Answer.filter(ar => ar.courseid === agr.courseid && ar.paaid === paa.id).result)
                fAnswers.foreach { allAnswers =>
                  for ((_, answers) <- allAnswers.groupBy(a => (a.userid, a.courseid, a.paaid))) checkDrawing(answers.maxBy(_.id), di, dgi)
                }
              case _ =>
            }
          case JsError(e) =>
            println("Error parsing spec from database. " + e)
        }
      }
    }.map(_ => AutoGradeResponse("Done grading.", true)) // TODO: Change this to indicate success if possible.
  }

  def checkMultipleChoice(ans: AnswerRow, mcgi: MultipleChoiceGradeInfo): Unit = {
    Json.fromJson[MultipleChoiceAnswer](Json.parse(ans.details)) match {
      case JsSuccess(mca, path) =>
        setGradeData(GradeData(-1, ans.userid, ans.courseid, ans.paaid, if (mca.answer == mcgi.correct) 100.0 else 0.0, "Autograded"))
      case JsError(e) =>
        println("Error parsing answer details from database. " + e)
    }
  }

  def checkDrawing(ans: AnswerRow, di: DrawingInfo, dgi: DrawingGradeInfo): Unit = {
    Json.fromJson[DrawingAnswer](Json.parse(ans.details)) match {
      case JsSuccess(da, path) =>
        val correct = DrawAnswerElement.checkEquals(di.initialElements, dgi.elements, da.elements)
        setGradeData(GradeData(-1, ans.userid, ans.courseid, ans.paaid, if (correct) 100.0 else 0.0, "Autograded"))
      case JsError(e) =>
        println("Error parsing answer details from database. " + e)
    }
  }

  def updateActive(courseid: Int, newActive: Boolean): Future[Int] = {
    db.run(Course.filter(_.id === courseid).map(_.active).update(newActive))
  }
}