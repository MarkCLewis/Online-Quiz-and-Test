package onlineclassroom.instructorcomponents

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

@react class CreateCourse extends Component {
  case class Props(userData: UserData, exitFunc: () => Unit, exitReloadFunc: () => Unit)
  case class State(name: String, semester: String, section: Int, students: String, message: String)

  def initialState: State = State("", "", 1, "", "")

  def render(): ReactElement = div (
    h2 ("Create Course"),
    "Course Name:",
    input (`type` := "text", value := state.name, onChange := (e => setState(state.copy(name = e.target.value)))),
    "(required)",
    br (),
    "Semester:",
    input (`type` := "text", value := state.semester, onChange := (e => setState(state.copy(semester = e.target.value.take(4))))),
    "(required)",
    br (),
    "Section:",
    input (`type` := "number", value := state.section.toString, onChange := (e => setState(state.copy(section = if (e.target.value.isEmpty) 0 else e.target.value.toInt)))),
    "(required)",
    br (),
    "Students: (You can copy the text from your class roster here.)",
    br(),
    textarea (value := state.students, onChange := (e => setState(state.copy(students = e.target.value)))),
    br (),
    button ("Done", onClick := (e => tryCreateCourse())),
    button ("Cancel", onClick := (e => props.exitFunc())),
    state.message
  )

  implicit val ec = ExecutionContext.global

  def tryCreateCourse(): Unit = {
    if (state.name.isEmpty) setState(state.copy(message = "Course name is required."))
    else if (state.semester.isEmpty) setState(state.copy(message = "Semester is required."))
    else {
      PostFetch.fetch("/createCourse", NewCourseData(state.name, state.semester, state.section, state.students),
        (worked: Boolean) => if (worked) {
              props.exitFunc()
            } else {
              setState(state.copy(message = "Course creation failed."))
            },
        e => setState(_.copy(message = "Error with JSON response from server.")))
    }
  }
}