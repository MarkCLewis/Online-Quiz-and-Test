package onlineclassroom

import org.scalajs.dom
import slinky.core.annotations.react
import slinky.core.Component
import slinky.core.facade.ReactElement
import slinky.web.html._
import scala.scalajs.js.Thenable.Implicits._
import play.api.libs.json._
import scala.scalajs.js.JSON
import scala.concurrent.ExecutionContext
import org.scalajs.dom.experimental._
import onlineclassroom._
import onlineclassroom.ReadsAndWrites._
import instructorcomponents._

// Functions Needed:
// *** Create course - Component
// *** Create user (only root can create an instructor)
// *** Show list of courses
// Show list of assessments
// Create assessment
// Add problem to assessment
// Show list of problems
// Create problem of each type


object InstructorPageMode extends Enumeration {
  val TopPage, ChangePassword, CreatCourse, DisplayCourse, DisplayCourseSummary, EditProblem, EditAssessment = Value
}

@react class InstructorPage extends Component {
  case class Props(userData: UserData)
  case class State(courses: Seq[CourseData], assessments: Seq[AssessmentData], problems: Seq[ProblemSpec], instructors: Seq[UserData],
    mode: InstructorPageMode.Value, message: String, selectedProblem: Option[ProblemSpec], selectedAssessment: Option[AssessmentData], 
    editType: String, selectedIndex: Int)

  override def componentDidMount() = {
    loadCourses()
    loadAssessments()
    loadProblems()
  }

  def initialState = State(Nil, Nil, Nil, Nil, InstructorPageMode.TopPage, "", None, None, "", -1)

  def render(): ReactElement = state.mode match {
    case InstructorPageMode.TopPage =>
      div (
        header (h1 ("Instructor page: " + props.userData.username) ),
        button ("Change Password", onClick := (e => setState(state.copy(mode = InstructorPageMode.ChangePassword)))),
        hr(),
        hr(),
        button ("Create Course", onClick := (e => setState(state.copy(mode = InstructorPageMode.CreatCourse)))),
        hr(),
        CreateUser(props.userData),
        hr(),
        h3 ("Courses"),
        div ( table (
          thead (
            tr (
              th ("Course"),
              th ("View Summary")
            )
          ),
          tbody (
            state.courses.zipWithIndex.map { case (course, index) =>
              tr (key := index.toString, 
                td (s"${course.name}-${course.semester}-${course.section}", 
                  onClick := (e => setState(state.copy(mode = InstructorPageMode.DisplayCourse, selectedIndex = index)))),
                td ("Summary", onClick := (e => setState(state.copy(mode = InstructorPageMode.DisplayCourseSummary, selectedIndex = index))))
              )
            }
          )
        ) ),
        hr(),
        h3 ("Assessments"),
        button ("Create", onClick := (e => setState(state.copy(mode = InstructorPageMode.EditAssessment, selectedAssessment = None, selectedIndex = -1)))),
        br(),
        "Filter:",
        br(),
        table (
          thead ( tr (th ("Name"), th ("Description"), th ("Auto-grade"))),
          tbody (
            state.assessments.zipWithIndex.map { case (a, i) =>
              tr ( key := i.toString, td (a.name), td (a.description), td (AutoGradeOptions.asString(a.autoGrade)), 
                onClick := (e => setState(state.copy(mode = InstructorPageMode.EditAssessment, selectedIndex = i, selectedAssessment = Some(a), editType = ""))))
            }
          )
        ),
        hr(),
        h3 ("Problems"),
        "Create: ",
        select (
          option (value := "", "Select To Create"),
          option (value := "SA", "Short Answer"),
          option (value := "MC", "Multiple Choice"),
          option (value := "Func", "Write Function"),
          option (value := "Expr", "Write Expression"),
          option (value := "Lambda", "Write Lambda"),
          option (value := "Drawing", "Drawing Only"),
          onChange := (e => if (e.target.value != "") {
            setState(state.copy(mode = InstructorPageMode.EditProblem, editType = e.target.value, selectedIndex = -1, selectedProblem = None))
          })
        ),
        br(),
        "Filter:",
        br(),
        table (
          thead (tr (th ("Type"), th ("Name"), th ("Prompt"))),
          tbody (
            state.problems.zipWithIndex.map { case (pspec, i) =>
              tr (key := i.toString, td (pspec.specType), td (pspec.info.name), td (pspec.info.prompt),
                onClick := (e => setState(state.copy(mode = InstructorPageMode.EditProblem, selectedIndex = i, selectedProblem = Some(pspec), editType = ""))))
            }
          )
        ),
        hr(),
        state.message
      )
    case InstructorPageMode.ChangePassword =>
      ChangePasswordComponent(props.userData, () => setState(state.copy(mode = InstructorPageMode.TopPage)))
    case InstructorPageMode.CreatCourse =>
      CreateCourse(props.userData, 
        () => setState(state.copy(mode = InstructorPageMode.TopPage)), 
        () => { loadCourses(); setState(state.copy(mode = InstructorPageMode.TopPage)) })
    case InstructorPageMode.DisplayCourse =>
      ViewCourse(props.userData, state.courses(state.selectedIndex), state.assessments, () => setState(state.copy(mode = InstructorPageMode.TopPage)))
    case InstructorPageMode.DisplayCourseSummary =>
      ViewCourseSummary(props.userData, state.courses(state.selectedIndex), () => setState(state.copy(mode = InstructorPageMode.TopPage)))
    case InstructorPageMode.EditProblem =>
      EditProblem(state.editType, state.selectedProblem, () => setState(state.copy(mode = InstructorPageMode.TopPage)), () => loadProblems())
    case InstructorPageMode.EditAssessment =>
      EditAssessment(state.selectedAssessment, state.problems, () => setState(state.copy(mode = InstructorPageMode.TopPage)), () => loadAssessments())
  }

  implicit val ec = ExecutionContext.global

  def loadCourses(): Unit = {
    PostFetch.fetch("/getCourses", props.userData.id,
      (cd: Seq[CourseData]) => setState(state.copy(courses = cd)),
      e => setState(_.copy(message = "Error with JSON loading courses.")))
  }

  def loadAssessments(): Unit = {
    PostFetch.fetch("getAssessments", props.userData.id,
      (ads: Seq[AssessmentData]) => setState(state.copy(assessments = ads)),
      e => setState(_.copy(message = "Error with JSON loading assessments.")))
    PostFetch.fetch("/getInstructors", props.userData.id,
      (instructors: Seq[UserData]) => setState(state.copy(instructors = instructors)),
      e => setState(_.copy(message = "Error with JSON loading instructors.")))
  }

  def loadProblems(): Unit = {
    PostFetch.fetch("/getProblems", props.userData.id,
      (probs: Seq[ProblemSpec]) => setState(state.copy(problems = probs)),
      e => setState(_.copy(message = "Error with Json loading problems.")))
  }

}