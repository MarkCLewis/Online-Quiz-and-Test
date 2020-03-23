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

object CourseViewMode extends Enumeration {
  val Normal, TakeAssessment = Value
}

@react class ViewCourse extends Component {
  case class Props(userData: UserData, course: CourseData, exitFunc: () => Unit)
  
  // TODO: Need FullStudentData for this course both for multiplier and for grades.
  case class State(message: String, mode: CourseViewMode.Value, assessments: Seq[AssessmentCourseInfo], starts: Seq[StudentAssessmentStart],
    selectedAssessment: Option[AssessmentCourseInfo], serverTime: Date)

  def initialState: State = State("", CourseViewMode.Normal, Nil, Nil, None, new Date)

  private var shortInterval: Int = 0;
  private var longInterval: Int = 0;

  implicit val ec = ExecutionContext.global

  override def componentDidMount(): Unit = {
    loadTime()
    loadData()
    shortInterval = dom.window.setInterval(() => updateTimer1Second(), 1000)
    longInterval = dom.window.setInterval(() => loadTime(), 60000)
  }

  override def componentWillUnmount(): Unit = {
    dom.window.clearInterval(shortInterval)
    dom.window.clearInterval(longInterval)
  }

  def render: ReactElement = {
    val startMap = state.starts.map(s => s.aciid -> s).toMap
    state.mode match {
      case CourseViewMode.Normal =>
        div (
          // TODO: Row tables with grades.
          "All times in the server's timezone.",
          br(),
          "Server Time:",
          state.serverTime.toLocaleString(),
          br(),
          h3 ("Assessments:"),
          "Click an assessment name to start that assessment.",
          br(),
          table (
            thead ( tr ( th ("Name"), th ("Start Time"), th ("End Time"), th ("Length [Minutes]"), th ("Status"))),
            tbody (
              state.assessments.zipWithIndex.map { case (a, i) =>
                val start = a.start.getOrElse("None")
                val end = a.end.getOrElse("None")
                val limit = a.timeLimit.map(_.toString).getOrElse("None")
                tr ( key := i.toString, 
                  td (a.name, onClick := (e => if (TimeMethods.assessmentViewable(a, state.serverTime)) setState(state.copy(mode = CourseViewMode.TakeAssessment, selectedAssessment = Some(a))))), 
                  td (start),
                  td (end),
                  td (limit),
                  td (if (TimeMethods.assessmentOpen(a, startMap.get(a.id), state.serverTime)) "Open" else "Closed") 
                )
              }
            )
          ),
          button ("Exit", onClick := (e => props.exitFunc())),
          state.message
        )
      case CourseViewMode.TakeAssessment =>
        TakeAssessment(props.userData, props.course, state.selectedAssessment.get, state.serverTime, startMap.get(state.selectedAssessment.get.id), 
          () => setState(state.copy(mode = CourseViewMode.Normal)))
    }
  }

  def loadTime(): Unit = {
    PostFetch.fetch("/getServerTime", props.userData.id,
      (time: String) => setState(state.copy(serverTime = new Date(time))),
      e => setState(_.copy(message = "Error with JSON response getting server time.")))
  }

  def loadData(): Unit = {
    PostFetch.fetch("/getCourseAssessments", (props.userData.id, props.course.id),
      (as: Seq[AssessmentCourseInfo]) => setState(state.copy(assessments = as)),
      e => setState(_.copy(message = "Error with JSON response getting assessments.")))
    PostFetch.fetch("/getStudentStarts", (props.userData.id, props.course.id),
      (starts: Seq[StudentAssessmentStart]) => setState(state.copy(starts = starts)),
      e => setState(_.copy(message = "Error with JSON response getting starts.")))
  }

  def updateTimer1Second(): Unit = {
    state.serverTime.setMilliseconds(state.serverTime.getMilliseconds()+1000)
    setState(state.copy(serverTime = state.serverTime))
  }
}