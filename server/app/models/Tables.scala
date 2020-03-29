package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.PostgresProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Answer.schema, AnswerGrade.schema, Assessment.schema, AssessmentCourseAssoc.schema, AssessmentStartTime.schema, Course.schema, GradeFormula.schema, Problem.schema, ProblemAssessmentAssoc.schema, UserCourseAssoc.schema, Users.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Answer
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param userid Database column userid SqlType(int4)
   *  @param courseid Database column courseid SqlType(int4)
   *  @param paaid Database column paaid SqlType(int4)
   *  @param submitTime Database column submit_time SqlType(timestamp without time zone)
   *  @param details Database column details SqlType(varchar), Length(20000,true) */
  case class AnswerRow(id: Int, userid: Int, courseid: Int, paaid: Int, submitTime: java.sql.Timestamp, details: String)
  /** GetResult implicit for fetching AnswerRow objects using plain SQL queries */
  implicit def GetResultAnswerRow(implicit e0: GR[Int], e1: GR[java.sql.Timestamp], e2: GR[String]): GR[AnswerRow] = GR{
    prs => import prs._
    AnswerRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[java.sql.Timestamp], <<[String]))
  }
  /** Table description of table answer. Objects of this class serve as prototypes for rows in queries. */
  class Answer(_tableTag: Tag) extends profile.api.Table[AnswerRow](_tableTag, "answer") {
    def * = (id, userid, courseid, paaid, submitTime, details) <> (AnswerRow.tupled, AnswerRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(userid), Rep.Some(courseid), Rep.Some(paaid), Rep.Some(submitTime), Rep.Some(details))).shaped.<>({r=>import r._; _1.map(_=> AnswerRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column userid SqlType(int4) */
    val userid: Rep[Int] = column[Int]("userid")
    /** Database column courseid SqlType(int4) */
    val courseid: Rep[Int] = column[Int]("courseid")
    /** Database column paaid SqlType(int4) */
    val paaid: Rep[Int] = column[Int]("paaid")
    /** Database column submit_time SqlType(timestamp without time zone) */
    val submitTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("submit_time")
    /** Database column details SqlType(varchar), Length(20000,true) */
    val details: Rep[String] = column[String]("details", O.Length(20000,varying=true))

    /** Foreign key referencing Course (database name answer_courseid_fkey) */
    lazy val courseFk = foreignKey("answer_courseid_fkey", courseid, Course)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing ProblemAssessmentAssoc (database name answer_paaid_fkey) */
    lazy val problemAssessmentAssocFk = foreignKey("answer_paaid_fkey", paaid, ProblemAssessmentAssoc)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Users (database name answer_userid_fkey) */
    lazy val usersFk = foreignKey("answer_userid_fkey", userid, Users)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Answer */
  lazy val Answer = new TableQuery(tag => new Answer(tag))

  /** Entity class storing rows of table AnswerGrade
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param answerid Database column answerid SqlType(int4)
   *  @param percentCorrect Database column percent_correct SqlType(float8)
   *  @param comments Database column comments SqlType(varchar), Length(200,true) */
  case class AnswerGradeRow(id: Int, answerid: Int, percentCorrect: Double, comments: String)
  /** GetResult implicit for fetching AnswerGradeRow objects using plain SQL queries */
  implicit def GetResultAnswerGradeRow(implicit e0: GR[Int], e1: GR[Double], e2: GR[String]): GR[AnswerGradeRow] = GR{
    prs => import prs._
    AnswerGradeRow.tupled((<<[Int], <<[Int], <<[Double], <<[String]))
  }
  /** Table description of table answer_grade. Objects of this class serve as prototypes for rows in queries. */
  class AnswerGrade(_tableTag: Tag) extends profile.api.Table[AnswerGradeRow](_tableTag, "answer_grade") {
    def * = (id, answerid, percentCorrect, comments) <> (AnswerGradeRow.tupled, AnswerGradeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(answerid), Rep.Some(percentCorrect), Rep.Some(comments))).shaped.<>({r=>import r._; _1.map(_=> AnswerGradeRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column answerid SqlType(int4) */
    val answerid: Rep[Int] = column[Int]("answerid")
    /** Database column percent_correct SqlType(float8) */
    val percentCorrect: Rep[Double] = column[Double]("percent_correct")
    /** Database column comments SqlType(varchar), Length(200,true) */
    val comments: Rep[String] = column[String]("comments", O.Length(200,varying=true))

    /** Foreign key referencing Answer (database name answer_grade_answerid_fkey) */
    lazy val answerFk = foreignKey("answer_grade_answerid_fkey", answerid, Answer)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table AnswerGrade */
  lazy val AnswerGrade = new TableQuery(tag => new AnswerGrade(tag))

  /** Entity class storing rows of table Assessment
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(varchar), Length(30,true)
   *  @param description Database column description SqlType(varchar), Length(2000,true)
   *  @param autoGrade Database column auto_grade SqlType(int4)
   *  @param creatorid Database column creatorid SqlType(int4), Default(27) */
  case class AssessmentRow(id: Int, name: String, description: String, autoGrade: Int, creatorid: Int = 27)
  /** GetResult implicit for fetching AssessmentRow objects using plain SQL queries */
  implicit def GetResultAssessmentRow(implicit e0: GR[Int], e1: GR[String]): GR[AssessmentRow] = GR{
    prs => import prs._
    AssessmentRow.tupled((<<[Int], <<[String], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table assessment. Objects of this class serve as prototypes for rows in queries. */
  class Assessment(_tableTag: Tag) extends profile.api.Table[AssessmentRow](_tableTag, "assessment") {
    def * = (id, name, description, autoGrade, creatorid) <> (AssessmentRow.tupled, AssessmentRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), Rep.Some(description), Rep.Some(autoGrade), Rep.Some(creatorid))).shaped.<>({r=>import r._; _1.map(_=> AssessmentRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(varchar), Length(30,true) */
    val name: Rep[String] = column[String]("name", O.Length(30,varying=true))
    /** Database column description SqlType(varchar), Length(2000,true) */
    val description: Rep[String] = column[String]("description", O.Length(2000,varying=true))
    /** Database column auto_grade SqlType(int4) */
    val autoGrade: Rep[Int] = column[Int]("auto_grade")
    /** Database column creatorid SqlType(int4), Default(27) */
    val creatorid: Rep[Int] = column[Int]("creatorid", O.Default(27))

    /** Foreign key referencing Users (database name assessment_creatorid_fkey) */
    lazy val usersFk = foreignKey("assessment_creatorid_fkey", creatorid, Users)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Assessment */
  lazy val Assessment = new TableQuery(tag => new Assessment(tag))

  /** Entity class storing rows of table AssessmentCourseAssoc
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param courseid Database column courseid SqlType(int4)
   *  @param assessmentid Database column assessmentid SqlType(int4)
   *  @param points Database column points SqlType(int4)
   *  @param gradeGroup Database column grade_group SqlType(varchar), Length(20,true)
   *  @param autoGrade Database column auto_grade SqlType(int4)
   *  @param startTime Database column start_time SqlType(timestamp without time zone), Default(None)
   *  @param endTime Database column end_time SqlType(timestamp without time zone), Default(None)
   *  @param timeLimit Database column time_limit SqlType(int4), Default(None) */
  case class AssessmentCourseAssocRow(id: Int, courseid: Int, assessmentid: Int, points: Int, gradeGroup: String, autoGrade: Int, startTime: Option[java.sql.Timestamp] = None, endTime: Option[java.sql.Timestamp] = None, timeLimit: Option[Int] = None)
  /** GetResult implicit for fetching AssessmentCourseAssocRow objects using plain SQL queries */
  implicit def GetResultAssessmentCourseAssocRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[java.sql.Timestamp]], e3: GR[Option[Int]]): GR[AssessmentCourseAssocRow] = GR{
    prs => import prs._
    AssessmentCourseAssocRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[String], <<[Int], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp], <<?[Int]))
  }
  /** Table description of table assessment_course_assoc. Objects of this class serve as prototypes for rows in queries. */
  class AssessmentCourseAssoc(_tableTag: Tag) extends profile.api.Table[AssessmentCourseAssocRow](_tableTag, "assessment_course_assoc") {
    def * = (id, courseid, assessmentid, points, gradeGroup, autoGrade, startTime, endTime, timeLimit) <> (AssessmentCourseAssocRow.tupled, AssessmentCourseAssocRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(courseid), Rep.Some(assessmentid), Rep.Some(points), Rep.Some(gradeGroup), Rep.Some(autoGrade), startTime, endTime, timeLimit)).shaped.<>({r=>import r._; _1.map(_=> AssessmentCourseAssocRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column courseid SqlType(int4) */
    val courseid: Rep[Int] = column[Int]("courseid")
    /** Database column assessmentid SqlType(int4) */
    val assessmentid: Rep[Int] = column[Int]("assessmentid")
    /** Database column points SqlType(int4) */
    val points: Rep[Int] = column[Int]("points")
    /** Database column grade_group SqlType(varchar), Length(20,true) */
    val gradeGroup: Rep[String] = column[String]("grade_group", O.Length(20,varying=true))
    /** Database column auto_grade SqlType(int4) */
    val autoGrade: Rep[Int] = column[Int]("auto_grade")
    /** Database column start_time SqlType(timestamp without time zone), Default(None) */
    val startTime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("start_time", O.Default(None))
    /** Database column end_time SqlType(timestamp without time zone), Default(None) */
    val endTime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("end_time", O.Default(None))
    /** Database column time_limit SqlType(int4), Default(None) */
    val timeLimit: Rep[Option[Int]] = column[Option[Int]]("time_limit", O.Default(None))

    /** Foreign key referencing Assessment (database name assessment_course_assoc_assessmentid_fkey) */
    lazy val assessmentFk = foreignKey("assessment_course_assoc_assessmentid_fkey", assessmentid, Assessment)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Course (database name assessment_course_assoc_courseid_fkey) */
    lazy val courseFk = foreignKey("assessment_course_assoc_courseid_fkey", courseid, Course)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table AssessmentCourseAssoc */
  lazy val AssessmentCourseAssoc = new TableQuery(tag => new AssessmentCourseAssoc(tag))

  /** Entity class storing rows of table AssessmentStartTime
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param userid Database column userid SqlType(int4)
   *  @param acaid Database column acaid SqlType(int4)
   *  @param timeStarted Database column time_started SqlType(timestamp without time zone) */
  case class AssessmentStartTimeRow(id: Int, userid: Int, acaid: Int, timeStarted: java.sql.Timestamp)
  /** GetResult implicit for fetching AssessmentStartTimeRow objects using plain SQL queries */
  implicit def GetResultAssessmentStartTimeRow(implicit e0: GR[Int], e1: GR[java.sql.Timestamp]): GR[AssessmentStartTimeRow] = GR{
    prs => import prs._
    AssessmentStartTimeRow.tupled((<<[Int], <<[Int], <<[Int], <<[java.sql.Timestamp]))
  }
  /** Table description of table assessment_start_time. Objects of this class serve as prototypes for rows in queries. */
  class AssessmentStartTime(_tableTag: Tag) extends profile.api.Table[AssessmentStartTimeRow](_tableTag, "assessment_start_time") {
    def * = (id, userid, acaid, timeStarted) <> (AssessmentStartTimeRow.tupled, AssessmentStartTimeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(userid), Rep.Some(acaid), Rep.Some(timeStarted))).shaped.<>({r=>import r._; _1.map(_=> AssessmentStartTimeRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column userid SqlType(int4) */
    val userid: Rep[Int] = column[Int]("userid")
    /** Database column acaid SqlType(int4) */
    val acaid: Rep[Int] = column[Int]("acaid")
    /** Database column time_started SqlType(timestamp without time zone) */
    val timeStarted: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("time_started")

    /** Foreign key referencing AssessmentCourseAssoc (database name assessment_start_time_acaid_fkey) */
    lazy val assessmentCourseAssocFk = foreignKey("assessment_start_time_acaid_fkey", acaid, AssessmentCourseAssoc)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Users (database name assessment_start_time_userid_fkey) */
    lazy val usersFk = foreignKey("assessment_start_time_userid_fkey", userid, Users)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table AssessmentStartTime */
  lazy val AssessmentStartTime = new TableQuery(tag => new AssessmentStartTime(tag))

  /** Entity class storing rows of table Course
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(varchar), Length(20,true)
   *  @param semester Database column semester SqlType(varchar), Length(4,true)
   *  @param section Database column section SqlType(int4) */
  case class CourseRow(id: Int, name: String, semester: String, section: Int)
  /** GetResult implicit for fetching CourseRow objects using plain SQL queries */
  implicit def GetResultCourseRow(implicit e0: GR[Int], e1: GR[String]): GR[CourseRow] = GR{
    prs => import prs._
    CourseRow.tupled((<<[Int], <<[String], <<[String], <<[Int]))
  }
  /** Table description of table course. Objects of this class serve as prototypes for rows in queries. */
  class Course(_tableTag: Tag) extends profile.api.Table[CourseRow](_tableTag, "course") {
    def * = (id, name, semester, section) <> (CourseRow.tupled, CourseRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), Rep.Some(semester), Rep.Some(section))).shaped.<>({r=>import r._; _1.map(_=> CourseRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(varchar), Length(20,true) */
    val name: Rep[String] = column[String]("name", O.Length(20,varying=true))
    /** Database column semester SqlType(varchar), Length(4,true) */
    val semester: Rep[String] = column[String]("semester", O.Length(4,varying=true))
    /** Database column section SqlType(int4) */
    val section: Rep[Int] = column[Int]("section")
  }
  /** Collection-like TableQuery object for table Course */
  lazy val Course = new TableQuery(tag => new Course(tag))

  /** Entity class storing rows of table GradeFormula
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param courseid Database column courseid SqlType(int4)
   *  @param gradeGroup Database column grade_group SqlType(varchar), Length(20,true)
   *  @param formula Database column formula SqlType(varchar), Length(1000,true) */
  case class GradeFormulaRow(id: Int, courseid: Int, gradeGroup: String, formula: String)
  /** GetResult implicit for fetching GradeFormulaRow objects using plain SQL queries */
  implicit def GetResultGradeFormulaRow(implicit e0: GR[Int], e1: GR[String]): GR[GradeFormulaRow] = GR{
    prs => import prs._
    GradeFormulaRow.tupled((<<[Int], <<[Int], <<[String], <<[String]))
  }
  /** Table description of table grade_formula. Objects of this class serve as prototypes for rows in queries. */
  class GradeFormula(_tableTag: Tag) extends profile.api.Table[GradeFormulaRow](_tableTag, "grade_formula") {
    def * = (id, courseid, gradeGroup, formula) <> (GradeFormulaRow.tupled, GradeFormulaRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(courseid), Rep.Some(gradeGroup), Rep.Some(formula))).shaped.<>({r=>import r._; _1.map(_=> GradeFormulaRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column courseid SqlType(int4) */
    val courseid: Rep[Int] = column[Int]("courseid")
    /** Database column grade_group SqlType(varchar), Length(20,true) */
    val gradeGroup: Rep[String] = column[String]("grade_group", O.Length(20,varying=true))
    /** Database column formula SqlType(varchar), Length(1000,true) */
    val formula: Rep[String] = column[String]("formula", O.Length(1000,varying=true))

    /** Foreign key referencing Course (database name grade_formula_courseid_fkey) */
    lazy val courseFk = foreignKey("grade_formula_courseid_fkey", courseid, Course)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table GradeFormula */
  lazy val GradeFormula = new TableQuery(tag => new GradeFormula(tag))

  /** Entity class storing rows of table Problem
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param spec Database column spec SqlType(varchar), Length(20000,true)
   *  @param creatorid Database column creatorid SqlType(int4), Default(27) */
  case class ProblemRow(id: Int, spec: String, creatorid: Int = 27)
  /** GetResult implicit for fetching ProblemRow objects using plain SQL queries */
  implicit def GetResultProblemRow(implicit e0: GR[Int], e1: GR[String]): GR[ProblemRow] = GR{
    prs => import prs._
    ProblemRow.tupled((<<[Int], <<[String], <<[Int]))
  }
  /** Table description of table problem. Objects of this class serve as prototypes for rows in queries. */
  class Problem(_tableTag: Tag) extends profile.api.Table[ProblemRow](_tableTag, "problem") {
    def * = (id, spec, creatorid) <> (ProblemRow.tupled, ProblemRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(spec), Rep.Some(creatorid))).shaped.<>({r=>import r._; _1.map(_=> ProblemRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column spec SqlType(varchar), Length(20000,true) */
    val spec: Rep[String] = column[String]("spec", O.Length(20000,varying=true))
    /** Database column creatorid SqlType(int4), Default(27) */
    val creatorid: Rep[Int] = column[Int]("creatorid", O.Default(27))

    /** Foreign key referencing Users (database name problem_creatorid_fkey) */
    lazy val usersFk = foreignKey("problem_creatorid_fkey", creatorid, Users)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Problem */
  lazy val Problem = new TableQuery(tag => new Problem(tag))

  /** Entity class storing rows of table ProblemAssessmentAssoc
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param assessmentid Database column assessmentid SqlType(int4)
   *  @param problemid Database column problemid SqlType(int4)
   *  @param weight Database column weight SqlType(float8)
   *  @param extraCredit Database column extra_credit SqlType(bool) */
  case class ProblemAssessmentAssocRow(id: Int, assessmentid: Int, problemid: Int, weight: Double, extraCredit: Boolean)
  /** GetResult implicit for fetching ProblemAssessmentAssocRow objects using plain SQL queries */
  implicit def GetResultProblemAssessmentAssocRow(implicit e0: GR[Int], e1: GR[Double], e2: GR[Boolean]): GR[ProblemAssessmentAssocRow] = GR{
    prs => import prs._
    ProblemAssessmentAssocRow.tupled((<<[Int], <<[Int], <<[Int], <<[Double], <<[Boolean]))
  }
  /** Table description of table problem_assessment_assoc. Objects of this class serve as prototypes for rows in queries. */
  class ProblemAssessmentAssoc(_tableTag: Tag) extends profile.api.Table[ProblemAssessmentAssocRow](_tableTag, "problem_assessment_assoc") {
    def * = (id, assessmentid, problemid, weight, extraCredit) <> (ProblemAssessmentAssocRow.tupled, ProblemAssessmentAssocRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(assessmentid), Rep.Some(problemid), Rep.Some(weight), Rep.Some(extraCredit))).shaped.<>({r=>import r._; _1.map(_=> ProblemAssessmentAssocRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column assessmentid SqlType(int4) */
    val assessmentid: Rep[Int] = column[Int]("assessmentid")
    /** Database column problemid SqlType(int4) */
    val problemid: Rep[Int] = column[Int]("problemid")
    /** Database column weight SqlType(float8) */
    val weight: Rep[Double] = column[Double]("weight")
    /** Database column extra_credit SqlType(bool) */
    val extraCredit: Rep[Boolean] = column[Boolean]("extra_credit")

    /** Foreign key referencing Assessment (database name problem_assessment_assoc_assessmentid_fkey) */
    lazy val assessmentFk = foreignKey("problem_assessment_assoc_assessmentid_fkey", assessmentid, Assessment)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Problem (database name problem_assessment_assoc_problemid_fkey) */
    lazy val problemFk = foreignKey("problem_assessment_assoc_problemid_fkey", problemid, Problem)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table ProblemAssessmentAssoc */
  lazy val ProblemAssessmentAssoc = new TableQuery(tag => new ProblemAssessmentAssoc(tag))

  /** Entity class storing rows of table UserCourseAssoc
   *  @param userid Database column userid SqlType(int4)
   *  @param courseid Database column courseid SqlType(int4)
   *  @param timeMultiplier Database column time_multiplier SqlType(float8) */
  case class UserCourseAssocRow(userid: Int, courseid: Int, timeMultiplier: Double)
  /** GetResult implicit for fetching UserCourseAssocRow objects using plain SQL queries */
  implicit def GetResultUserCourseAssocRow(implicit e0: GR[Int], e1: GR[Double]): GR[UserCourseAssocRow] = GR{
    prs => import prs._
    UserCourseAssocRow.tupled((<<[Int], <<[Int], <<[Double]))
  }
  /** Table description of table user_course_assoc. Objects of this class serve as prototypes for rows in queries. */
  class UserCourseAssoc(_tableTag: Tag) extends profile.api.Table[UserCourseAssocRow](_tableTag, "user_course_assoc") {
    def * = (userid, courseid, timeMultiplier) <> (UserCourseAssocRow.tupled, UserCourseAssocRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userid), Rep.Some(courseid), Rep.Some(timeMultiplier))).shaped.<>({r=>import r._; _1.map(_=> UserCourseAssocRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column userid SqlType(int4) */
    val userid: Rep[Int] = column[Int]("userid")
    /** Database column courseid SqlType(int4) */
    val courseid: Rep[Int] = column[Int]("courseid")
    /** Database column time_multiplier SqlType(float8) */
    val timeMultiplier: Rep[Double] = column[Double]("time_multiplier")

    /** Primary key of UserCourseAssoc (database name user_course_assoc_pkey) */
    val pk = primaryKey("user_course_assoc_pkey", (userid, courseid))

    /** Foreign key referencing Course (database name user_course_assoc_courseid_fkey) */
    lazy val courseFk = foreignKey("user_course_assoc_courseid_fkey", courseid, Course)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Users (database name user_course_assoc_userid_fkey) */
    lazy val usersFk = foreignKey("user_course_assoc_userid_fkey", userid, Users)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table UserCourseAssoc */
  lazy val UserCourseAssoc = new TableQuery(tag => new UserCourseAssoc(tag))

  /** Entity class storing rows of table Users
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param email Database column email SqlType(varchar), Length(20,true)
   *  @param password Database column password SqlType(varchar), Length(200,true)
   *  @param instructor Database column instructor SqlType(bool) */
  case class UsersRow(id: Int, email: String, password: String, instructor: Boolean)
  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUsersRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean]): GR[UsersRow] = GR{
    prs => import prs._
    UsersRow.tupled((<<[Int], <<[String], <<[String], <<[Boolean]))
  }
  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends profile.api.Table[UsersRow](_tableTag, "users") {
    def * = (id, email, password, instructor) <> (UsersRow.tupled, UsersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(email), Rep.Some(password), Rep.Some(instructor))).shaped.<>({r=>import r._; _1.map(_=> UsersRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column email SqlType(varchar), Length(20,true) */
    val email: Rep[String] = column[String]("email", O.Length(20,varying=true))
    /** Database column password SqlType(varchar), Length(200,true) */
    val password: Rep[String] = column[String]("password", O.Length(200,varying=true))
    /** Database column instructor SqlType(bool) */
    val instructor: Rep[Boolean] = column[Boolean]("instructor")
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
}
