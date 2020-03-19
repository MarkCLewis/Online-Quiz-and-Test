package onlineclassroom

sealed trait ClientToServerMessage
case class LoginData(username: String, password: String) extends ClientToServerMessage
case class NewCourseData(name:String,semester:String,section:Int,studentData:String)

case class CourseInfo(id: Int, name: String, semester: String, section: Int)

// TODO: Borrowed - check if they can be refactored
case class UserData(username: String, id: Int, sessionUID: Int)

object AutoGradeOptions extends Enumeration {
  val Never, OnProblemSubmit, OnTestSubmit = Value
}

case class Assessment(id: Int, name: String, description: String, points: Int, autoGrade: AutoGradeOptions.Value)

/**
 * The subtypes of ProblemAnswer are passed back from the client to the server with the user's answer.
 * They are also passed from the server to the client to give information on the previous answers.
 */
sealed trait ProblemAnswer
sealed trait CodeAnswer extends ProblemAnswer
case class MultipleChoiceAnswer(answer: Int) extends ProblemAnswer
case class ShortAnswerAnswer(text: String, elements: Seq[DrawAnswerElement]) extends ProblemAnswer
case class WriteFunctionAnswer(code: String) extends CodeAnswer
case class WriteLambdaAnswer(code: String) extends CodeAnswer
case class WriteExpressionAnswer(code: String) extends CodeAnswer
case class DrawingAnswer(elements: Seq[DrawAnswerElement]) extends ProblemAnswer

/**
 * The subtypes of ProblemInfo are passed from the server to the client for displaying during an assessment.
 */
sealed trait ProblemInfo {
  def prompt: String
}
case class MultipleChoiceInfo(prompt: String, options: Seq[Int]) extends ProblemInfo
case class ShortAnswerInfo(prompt: String, initialElements: Seq[DrawAnswerElement]) extends ProblemInfo
case class WriteFunctionInfo(prompt: String, functionName: String, varSpecs: Seq[VariableSpec]) extends ProblemInfo
case class WriteLambdaInfo(prompt: String, returnType: String, varSpecs: Seq[VariableSpec]) extends ProblemInfo
case class WriteExpressionInfo(prompt: String, varSpecs: Seq[VariableSpec], generalSetup: String) extends ProblemInfo
case class DrawingInfo(prompt: String, initialElements: Seq[DrawAnswerElement])

/**
 * These are the types that hold just the answer related part of a spec.
 */
sealed trait ProblemGradeInfo {
  def autoGradable: Boolean
}

case class MultipleChoiceGradeInfo(prompt: String, options: Seq[String], correct: Int) extends ProblemGradeInfo {
  def autoGradable: Boolean = true
}

case object ShortAnswerGradeInfo extends ProblemGradeInfo {
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

/**
 * The subtypes of ProblemSpec are passed between client and server when an instructor is editing them. They
 * contain the full information needed to run the problem.
 */
case class ProblemSpec(info: ProblemInfo, answerInfo: ProblemGradeInfo)

case class ProblemWrapper(id: Int, weight: Double, spec: ProblemSpec)
