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

@react class ViewAssessment extends Component {
  case class Props(userData: UserData, course: CourseData, aci: AssessmentCourseInfo, exitFunc: () => Unit)
  case class State(message: String, gradingData: Option[AssessmentGradingData])

  def initialState: State = State("", None)

  
  override def componentDidMount(): Unit = loadData()
  
  def render: ReactElement = {
    div (
      button ("Done", onClick := (e => props.exitFunc())),
      state.gradingData.map(agd =>
        div (
          h2 (agd.name),
          agd.description,
          hr (),
          agd.problems.sortBy(_.id).zipWithIndex.map { case (gpd, i) =>
            div ( key := i.toString,
              (gpd.spec.info, gpd.answers.lastOption.map(_.answer)) match {
                case (sai: ShortAnswerInfo, lastAnswer: Option[ShortAnswerAnswer]) =>
                  ShortAnswerQuestion(props.userData, props.course, -1, sai, lastAnswer, false, ans => {})
                case (mci: MultipleChoiceInfo, lastAnswer: Option[MultipleChoiceAnswer]) =>
                  MultipleChoiceQuestion(props.userData, props.course, -1, mci, lastAnswer, false, ans => {})
                case (wfi: WriteFunctionInfo, lastAnswer: Option[WriteFunctionAnswer]) =>
                  WriteFunctionQuestion(props.userData, props.course, -1, wfi, lastAnswer, false, ans => {})
                case (wei: WriteExpressionInfo, lastAnswer: Option[WriteExpressionAnswer]) =>
                  WriteExpressionQuestion(props.userData, props.course, -1, wei, lastAnswer, false, ans => {})
                case (wli: WriteLambdaInfo, lastAnswer: Option[WriteLambdaAnswer]) =>
                  WriteLambdaQuestion(props.userData, props.course, -1, wli, lastAnswer, false, ans => {})
                case (di: DrawingInfo, lastAnswer: Option[DrawingAnswer]) =>
                  DrawingQuestion(props.userData, props.course, -1, di, lastAnswer, false, ans => {})
              },
              br (),
              "Percent: ",
              gpd.grades.lastOption.map(_.percentCorrect.toString),
              br (),
              "Comments: ",
              gpd.grades.lastOption.map(_.comments),
              hr ()
            )
          }
        )
      ),
      button ("Done", onClick := (e => props.exitFunc()))
    )
  }

  implicit val ec = ExecutionContext.global

  def loadData(): Unit = {
    PostFetch.fetch("/studentAssessmentGradingData", (props.userData.id, props.course.id, props.aci.assessmentid),
      (adg: AssessmentGradingData) => setState(state.copy(gradingData = Some(adg))),
      e => setState(_.copy(message = "Error with Json loading assessment grading data.")))
  }
}
