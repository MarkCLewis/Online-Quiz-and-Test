package onlineclassroom.instructorcomponents

import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import scala.concurrent.ExecutionContext
import org.scalajs.dom.experimental._
import play.api.libs.json._
import scala.scalajs.js.Thenable.Implicits._
import onlineclassroom._
import onlineclassroom.ReadsAndWrites._

@react class GradeAssessment extends Component {
  case class Props(userData: UserData, course: CourseData, aci: AssessmentCourseInfo, exitFunc: () => Unit)
  case class State(message: String)

  def initialState: State = State("")

  override def componentDidMount(): Unit = {} //loadAssociatedProblems()

  def render: ReactElement = {
    div (
      button ("Exit", onClick := (e => props.exitFunc()))
    )
  }
}