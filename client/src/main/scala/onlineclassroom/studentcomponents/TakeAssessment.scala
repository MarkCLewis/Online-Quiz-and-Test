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
  case class Props(userData: UserData, course: CourseData, aci: AssessmentCourseInfo, initialServerTime: Date, 
    initialStart: Option[StudentAssessmentStart], exitFunc: () => Unit, multipler: Double)
  case class State(message: String, start: Option[StudentAssessmentStart], problems: Seq[StudentProblemSpec], serverTime: Date)

  def initialState: State = State("", props.initialStart, Nil, props.initialServerTime)

  private var shortInterval: Int = 0
  private var longInterval: Int = 0

  implicit val ec = ExecutionContext.global

  override def componentDidMount(): Unit = {
    loadData()
    loadTime()
    if (state.start.isEmpty && props.aci.timeLimit.nonEmpty) startTime()
    shortInterval = dom.window.setInterval(() => updateTimer1Second(), 1000)
    longInterval = dom.window.setInterval(() => loadTime(), 60000)
  }

  override def componentWillUnmount(): Unit = {
    dom.window.clearInterval(shortInterval)
    dom.window.clearInterval(longInterval)
  }

  def render: ReactElement = {
    val open = TimeMethods.assessmentOpen(props.aci, state.start, state.serverTime, props.multipler)
    val remainingMilis = TimeMethods.timeRemaining(props.aci, state.start, state.serverTime, props.multipler)
    val remainingStr = TimeMethods.millisToHMS(remainingMilis)
    div (
      h1 (props.aci.name),
      h2 (props.aci.description),
      (if (open) "Time left: " + remainingStr else ""),
      br (),
      state.problems.zipWithIndex.map { case (p, i) =>
        (p.info, p.answer) match {
          case (sai: ShortAnswerInfo, saa: Option[ShortAnswerAnswer]) =>
            div ( key := i.toString, 
              h3 (s"Problem ${i+1}"),
              ShortAnswerQuestion(props.userData, props.course, p.paaid, sai, saa, open, 
                newsaa => setState(state.copy(problems = state.problems.patch(i, Seq(p.copy(answer = Some(newsaa))), 1))) ),
              hr()
            )
          case (mci: MultipleChoiceInfo, mca: Option[MultipleChoiceAnswer]) =>
            div ( key := i.toString,
              h3 (s"Problem ${i+1}"),
              MultipleChoiceQuestion(props.userData, props.course, p.paaid, mci, mca, open, 
                newsaa => setState(state.copy(problems = state.problems.patch(i, Seq(p.copy(answer = Some(newsaa))), 1))) ),
              hr()
            )
          case (wfi: WriteFunctionInfo, wfa: Option[WriteFunctionAnswer]) =>
            div ( key := i.toString,
              h3 (s"Problem ${i+1}"),
              WriteFunctionQuestion(props.userData, props.course, p.paaid, wfi, wfa, open, 
                newsaa => setState(state.copy(problems = state.problems.patch(i, Seq(p.copy(answer = Some(newsaa))), 1))) ),
              hr()
            )
          case (wei: WriteExpressionInfo, wea: Option[WriteExpressionAnswer]) =>
            div ( key := i.toString,
              h3 (s"Problem ${i+1}"),
              WriteExpressionQuestion(props.userData, props.course, p.paaid, wei, wea, open, 
                newsaa => setState(state.copy(problems = state.problems.patch(i, Seq(p.copy(answer = Some(newsaa))), 1))) ),
              hr()
            )
          case (wli: WriteLambdaInfo, wla: Option[WriteLambdaAnswer]) =>
            div ( key := i.toString,
              h3 (s"Problem ${i+1}"),
              WriteLambdaQuestion(props.userData, props.course, p.paaid, wli, wla, open, 
                newsaa => setState(state.copy(problems = state.problems.patch(i, Seq(p.copy(answer = Some(newsaa))), 1))) ),
              hr()
            )
          case (di: DrawingInfo, da: Option[DrawingAnswer]) =>
            div ( key := i.toString,
              h3 (s"Problem ${i+1}"),
              DrawingQuestion(props.userData, props.course, p.paaid, di, da, open, 
                newsaa => setState(state.copy(problems = state.problems.patch(i, Seq(p.copy(answer = Some(newsaa))), 1))) ),
              hr()
            )
        }
      },
      button ("Done", onClick := (e => props.exitFunc()))
    )
  }

  def loadData(): Unit = {
    PostFetch.fetch("/getAssessmentProblems", (props.userData.id, props.course.id, props.aci.assessmentid, props.aci.id),
      (problems: Seq[StudentProblemSpec]) => setState(state.copy(problems = problems)),
      e => setState(_.copy(message = "Error with JSON response getting problems.")))
  }

  def loadTime(): Unit = {
    PostFetch.fetch("/getServerTime", props.userData.id,
      (time: String) => setState(state.copy(serverTime = new Date(time))),
      e => setState(_.copy(message = "Error with JSON response getting server time.")))
  }

  def updateTimer1Second(): Unit = {
    state.serverTime.setMilliseconds(state.serverTime.getMilliseconds()+1000)
    setState(state.copy(serverTime = state.serverTime))
  }

  def startTime(): Unit = {
    for (limit <- props.aci.timeLimit) {
      PostFetch.fetch("/startAssessment", (props.userData.id, props.aci.id),
        (start: StudentAssessmentStart) => setState(state.copy(start = Some(start))),
        e => setState(_.copy(message = "Error with JSON response setting start time.")))
    }
  }
}
