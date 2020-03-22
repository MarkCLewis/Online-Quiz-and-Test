package onlineclassroom

import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import scala.concurrent.ExecutionContext
import org.scalajs.dom.experimental._
import play.api.libs.json._
import scala.scalajs.js.Thenable.Implicits._
import ReadsAndWrites._
import studentcomponents._

// Functions Needed:
// See ist of 
object StudentPageMode extends Enumeration {
  val CourseList, ChangePassword, ViewCourse = Value
}

@react class StudentPage extends Component {
  case class Props(userData: UserData)
  case class State(courses: Seq[CourseData], mode: StudentPageMode.Value, selectedCourse: Option[CourseData], message: String)

  override def componentDidMount() = loadCourses()

  def initialState = State(Nil, StudentPageMode.CourseList, None, "")

  def render(): ReactElement = {
    state.mode match {
      case StudentPageMode.CourseList =>
        div (
          header (h1 ("User page: " + props.userData.username) ),
          button ("Change Password", onClick := (e => setState(state.copy(mode = StudentPageMode.ChangePassword)))),
          hr(),
          h3 ("Courses"),
          "Click a course to view it.",
          br (),
          ul (
            state.courses.zipWithIndex.map { case (cd, i) => li ( key := i.toString, cd.name, 
              onClick := (e => setState(state.copy(mode = StudentPageMode.ViewCourse, selectedCourse = Some(cd)))) ) }
          )
        )
      case StudentPageMode.ChangePassword =>
        ChangePasswordComponent(props.userData, () => setState(state.copy(mode = StudentPageMode.CourseList)))
      case StudentPageMode.ViewCourse =>
        ViewCourse(props.userData, state.selectedCourse.get, () => setState(state.copy(mode = StudentPageMode.CourseList)))
    }
  }
  
  implicit val ec = ExecutionContext.global

  def loadCourses(): Unit = {
    PostFetch.fetch("/getCourses", props.userData.id,
      (cd: Seq[CourseData]) => setState(state.copy(courses = cd)),
      e => setState(_.copy(message = "Error with JSON response getting courses.")))
  }
}