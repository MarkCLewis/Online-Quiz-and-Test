package onlineclassroom

object AutoGradeOptions {
  val Never = 0
  val OnProblemSubmit = 1
  val OnTestSubmit = 2
  val asString = Array("Never", "Problem Submit", "Test Submit")
  val fromString = Map("Never" -> Never, "Problem Submit" -> OnProblemSubmit, "Test Submit" -> OnTestSubmit)
}

case class LoginData(username: String, password: String)
case class NewCourseData(name:String, semester:String, section:Int, studentData:String)

case class CourseData(id: Int, name: String, semester: String, section: Int)

case class UserData(username: String, id: Int, instructor: Boolean)
case class PasswordChangeData(userid: Int, oldPassword: String, newPassword: String)
case class NewUserData(username: String, password: String, instructor: Boolean)

case class FullStudentData(id: Int, email: String, grades: Map[String, Double], timeMultiplier: Double)
case class AssessmentCourseInfo(id: Int, courseid: Int, assessmentid: Int, name: String, description: String, points: Int, group: String, autoGrade: Int, start: Option[String], end: Option[String], timeLimit: Option[Int])
case class GradeFormulaInfo(id: Int, groupName: String, formula: String)
case class CourseGradeInformation(assessments: Seq[AssessmentCourseInfo], formulas: Seq[GradeFormulaInfo])
case class FullInstructorCourseData(students: Seq[FullStudentData], grades: CourseGradeInformation)

case class GradeData(id: Int, answerid: Int, percentCorrect: Double, comments: String)
case class GradeAnswer(id: Int, userid: Int, courseid: Int, paaid: Int, submitTime: String, answer: ProblemAnswer, gradeData: Option[GradeData])
case class GradingProblemData(id: Int, spec: ProblemSpec, answers: Seq[GradeAnswer])
case class AssessmentGradingData(id: Int, name: String, description: String, problems: Seq[GradingProblemData])

case class StudentAssessmentStart(id: Int, userid: Int, aciid: Int, timeStarted: String)

case class AssessmentData(id: Int, name: String, description: String, autoGrade: Int)

/**
 * The subtypes of ProblemAnswer are passed back from the client to the server with the user's answer.
 * They are also passed from the server to the client to give information on the previous answers.
 */
sealed trait ProblemAnswer
case class MultipleChoiceAnswer(answer: Int) extends ProblemAnswer
case class ShortAnswerAnswer(text: String, elements: Seq[DrawAnswerElement]) extends ProblemAnswer
case class WriteFunctionAnswer(code: String) extends ProblemAnswer
case class WriteLambdaAnswer(code: String) extends ProblemAnswer
case class WriteExpressionAnswer(code: String) extends ProblemAnswer
case class DrawingAnswer(elements: Seq[DrawAnswerElement]) extends ProblemAnswer
case class ManualEntryAnswer(requiredForJson: Int = -1) extends ProblemAnswer
case class ProblemAnswerError(error: String) extends ProblemAnswer

/**
 * The subtypes of ProblemInfo are passed from the server to the client for displaying during an assessment.
 */
sealed trait ProblemInfo {
  def name: String
  def prompt: String
}
case class MultipleChoiceInfo(name: String, prompt: String, options: Seq[Int]) extends ProblemInfo
case class ShortAnswerInfo(name: String, prompt: String, initialElements: Seq[DrawAnswerElement]) extends ProblemInfo
case class WriteFunctionInfo(name: String, prompt: String, functionName: String, varSpecs: Seq[VariableSpec]) extends ProblemInfo
case class WriteLambdaInfo(name: String, prompt: String, returnType: String, varSpecs: Seq[VariableSpec]) extends ProblemInfo
case class WriteExpressionInfo(name: String, prompt: String, varSpecs: Seq[VariableSpec], generalSetup: String) extends ProblemInfo
case class DrawingInfo(name: String, prompt: String, initialElements: Seq[DrawAnswerElement]) extends ProblemInfo
case class ManualEntryInfo(name: String) extends ProblemInfo { def prompt = "Manual Entry" }
case class ProblemInfoError(name: String, prompt: String) extends ProblemInfo

/**
 * These are the types that hold just the answer related part of a spec.
 */
sealed trait ProblemGradeInfo {
  def autoGradable: Boolean
}

case class MultipleChoiceGradeInfo(correct: Int) extends ProblemGradeInfo {
  def autoGradable: Boolean = true
}

case class ShortAnswerGradeInfo(requiredForJson: Int = -1) extends ProblemGradeInfo {
  def autoGradable: Boolean = false
}

case class WriteFunctionGradeInfo(correctCode: String, numRuns: Int) extends ProblemGradeInfo {
  def autoGradable: Boolean = true
}

case class WriteLambdaGradeInfo(correctCode: String, numRuns: Int) extends ProblemGradeInfo {
  def autoGradable: Boolean = true
}

case class WriteExpressionGradeInfo(correctCode: String, numRuns: Int) extends ProblemGradeInfo {
  def autoGradable: Boolean = true
}

// My current thought here is that each entry has the label on a ref that links to a nested structure that
// indicates what should be pointed to. For example:
// root -> D 5 (D 3 (D 2) (D 4)) (D 7 (D 6) (D 9)) -- for a binary tree
// head -> S 3 (S 2 (S 1))
// arr -> A 1 2 3 4 5
// arr -> A (V 32) (V 42)
case class DrawingGradeInfo(structs: Seq[(String, String)]) extends ProblemGradeInfo {
  def autoGradable: Boolean = true
}

case class ManualEntryGradeInfo(requiredForJson: Int = -1) extends ProblemGradeInfo {
  def autoGradable: Boolean = false
}
/**
 * ProblemSpec is passed between client and server when an instructor is editing them. They
 * contain the full information needed to run the problem.
 */
case class ProblemSpec(id: Int, info: ProblemInfo, answerInfo: ProblemGradeInfo) {
  def specType: String = (info, answerInfo) match {
    case (_:ShortAnswerInfo, _:ShortAnswerGradeInfo) => "Short Answer"
    case (_:MultipleChoiceInfo, _:MultipleChoiceGradeInfo) => "Multiple Choice"
    case (_:WriteFunctionInfo, _:WriteFunctionGradeInfo) => "Function"
    case (_:WriteLambdaInfo, _:WriteLambdaGradeInfo) => "Lambda"
    case (_:WriteExpressionInfo, _:WriteExpressionGradeInfo) => "Expression"
    case (_:DrawingInfo, _:DrawingGradeInfo) => "Drawing"
    case (_:ManualEntryInfo, _:ManualEntryGradeInfo) => "Manual Entry"
    case _ => 
      println(s"Problem Spec mismatch:\n  $info\n  $answerInfo")
      "Mismatch"
  }
}

// case class ProblemSpecWithAnswers(id: Int, )

case class StudentProblemSpec(paaid: Int, assessmentid: Int, problemid: Int, weight: Double, extraCredit: Boolean, info: ProblemInfo, answer: Option[ProblemAnswer])

case class SaveAnswerInfo(id: Int, userid: Int, courseid: Int, paaid: Int, answer: ProblemAnswer)

case class ProblemAssessmentAssociation(id: Int, assessmentid: Int, problemid: Int, weight: Double, extraCredit: Boolean)

