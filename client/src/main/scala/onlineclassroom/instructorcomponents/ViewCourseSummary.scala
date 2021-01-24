package onlineclassroom.instructorcomponents

import scalajs.js
import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import scala.concurrent.ExecutionContext
import onlineclassroom._
import onlineclassroom.ReadsAndWrites._

object InstructorCourseSummaryViewModes extends Enumeration {
  val Normal, Summary = Value
}

@react class ViewCourseSummary extends Component {
  case class Props(userData: UserData, course: CourseData, allAssessments: Seq[AssessmentData], exitFunc: () => Unit)
  case class State(message: String, mode: InstructorCourseSummaryViewModes.Value, gradeData: Option[AssessmentGradingData], 
    selectedAssessment: Option[AssessmentData])

  def initialState: State = State("", InstructorCourseSummaryViewModes.Normal, None, None)

  override def componentDidUpdate(prevProps: Props, prevState: State): Unit = {
    if (prevState.selectedAssessment != state.selectedAssessment) {
      loadData()
    }
  }

  def render: ReactElement = {
    state.mode match {
      case InstructorCourseSummaryViewModes.Normal =>
        div (
          // Table listing assessment data.
          table (
            thead (
              tr ( th ("Assessment"), th ("Auto Grade"))
            ),
            tbody (
              props.allAssessments.map { ad =>
                println(ad)
                tr ( key := s"key-${ad.id}",
                  td ( ad.name,
                    onClick := (e => setState(state.copy(mode = InstructorCourseSummaryViewModes.Summary, selectedAssessment = Some(ad))))
                  ),
                  td ( if (ad.autoGrade == AutoGradeOptions.OnInstructor) {
                    span ("Grade", onClick := (e => autoGrade(ad.id)))
                  } else "")
                )
              }
            )
          ),
          state.message,
          button ("Done", onClick := (e => props.exitFunc()))
        )
      case InstructorCourseSummaryViewModes.Summary =>
        state.gradeData match {
          case None =>
            div (
              "Loading Course Data ...", state.message,
              br (),
              button ("Done", onClick := (e => setState(state.copy(mode = InstructorCourseSummaryViewModes.Normal))))
            )
          case Some(gd) =>
            div (
              // Display questions with summary information (options and selected for MC, submissions and correct for code)
              h2 (gd.name),
              gd.description,
              br(),
              table (
                thead (
                  tr ( th ("Problem"), th ("Answer Info") )
                ),
                tbody (
                  gd.problems.map { gpd => 
                    tr ( key := s"key-${gpd.id}",
                      td ( 
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
                        }
                      ),
                      td (
                        s"${gpd.grades.count(_.percentCorrect == 100)} / ${gpd.grades.length}"
                      )
                    )
                  }
                )
              ),
              button ("Done", onClick := (e => setState(state.copy(mode = InstructorCourseSummaryViewModes.Normal))))
            ) 
        }
    }
  }

  implicit val ec = ExecutionContext.global

  def loadData(): Unit = {
    println("Loading data")
    state.selectedAssessment.foreach(sa =>
      PostFetch.fetch("/assessmentGradingData", (props.course.id, sa.id),
        (agd: AssessmentGradingData) => {println("Data loaded."); setState(state.copy(gradeData = Some(agd)))},
        e => { println(e); setState(state.copy(message = "Error with JSON loading data."))})
    )
  }

  def autoGrade(aid: Int): Unit = {
    PostFetch.fetch("/autoGrade", AutoGradeRequest(props.course.id, aid),
      (agr: AutoGradeResponse) => setState(state.copy(message = agr.message)),
      e => { println(e); setState(state.copy(message = "Error with JSON in autograde."))})
  }
}