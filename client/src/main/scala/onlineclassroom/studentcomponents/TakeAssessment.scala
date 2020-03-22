package onlineclassroom.studentcomponents

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
import scala.scalajs.js.Date

@react class TakeAssessment extends Component {
  case class Props(userData: UserData, course: CourseData, aci: AssessmentCourseInfo, initialServerTime: Date, initialState: Option[StudentAssessmentStart], exitFunc: () => Unit)
  case class State(message: String)

  def initialState: State = State("")

  override def componentDidMount(): Unit = {
    loadData()
  }

  def render: ReactElement = {
    div (
      h2 (props.aci.name),
      h3 (props.aci.description),
      button ("Exit", onClick := (e => props.exitFunc()))
    )
  }

  implicit val ec = ExecutionContext.global

  def loadData(): Unit = {

  }
}
