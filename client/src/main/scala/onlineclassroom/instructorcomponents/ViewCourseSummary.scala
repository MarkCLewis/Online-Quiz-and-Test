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
  case class Props(userData: UserData, course: CourseData, exitFunc: () => Unit)
  case class State(message: String, mode: InstructorCourseSummaryViewModes.Value, courseGradeInfo: Option[CourseGradeInformation],
    gradeData: Option[AssessmentGradingData], selectedAssessment: Option[AssessmentCourseInfo])

  def initialState: State = State("", InstructorCourseSummaryViewModes.Normal, None, None, None)

  override def componentDidMount() = {
    loadCourseGradeInformation()
  }

  override def componentDidUpdate(prevProps: Props, prevState: State): Unit = {
    if (prevState.selectedAssessment != state.selectedAssessment) {
      loadDataAssessment()
    }
  }

  def render: ReactElement = {
    state.mode match {
      case InstructorCourseSummaryViewModes.Normal =>
        state.courseGradeInfo match {
          case None => div (
            "Loading data...",
            button ("Done", onClick := (e => props.exitFunc()))
          )
          case Some(cgi) =>
            div (
              // Table listing assessment data.
              table (
                thead (
                  tr ( th ("Assessment"), th ("Auto Grade"))
                ),
                tbody (
                  cgi.assessments.map { aci =>
                    tr ( key := s"key-${aci.id}",
                      td ( aci.name,
                        onClick := (e => setState(state.copy(message = "", mode = InstructorCourseSummaryViewModes.Summary, selectedAssessment = Some(aci))))
                      ),
                      td ( if (aci.autoGrade == AutoGradeOptions.OnInstructor) {
                        span ("Grade", onClick := (e => autoGrade(aci.assessmentid)))
                      } else "")
                    )
                  }
                )
              ),
              state.message,
              button ("Done", onClick := (e => props.exitFunc()))
            )
        }
      case InstructorCourseSummaryViewModes.Summary =>
        state.gradeData match {
          case None =>
            div (
              "Loading Course Data ...", state.message,
              br (),
              button ("Done", onClick := (e => setState(state.copy(message = "", mode = InstructorCourseSummaryViewModes.Normal))))
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
                        (gpd.spec.info, gpd.spec.answerInfo) match {
                          case (sai: ShortAnswerInfo, answerInfo: ShortAnswerGradeInfo) =>
                            ShortAnswerQuestion(props.userData, props.course, -1, sai, Some(ShortAnswerAnswer("", sai.initialElements)), false, ans => {})
                          case (mci: MultipleChoiceInfo, answerInfo: MultipleChoiceGradeInfo) =>
                            MultipleChoiceQuestion(props.userData, props.course, -1, mci, Some(MultipleChoiceAnswer(answerInfo.correct)), false, ans => {})
                          case (wfi: WriteFunctionInfo, _) =>
                            WriteFunctionQuestion(props.userData, props.course, -1, wfi, None, false, ans => {})
                          case (wei: WriteExpressionInfo, _) =>
                            WriteExpressionQuestion(props.userData, props.course, -1, wei, None, false, ans => {})
                          case (wli: WriteLambdaInfo, _) =>
                            WriteLambdaQuestion(props.userData, props.course, -1, wli, None, false, ans => {})
                          case (di: DrawingInfo, answerInfo: DrawingGradeInfo) =>
                            DrawingQuestion(props.userData, props.course, -1, di, Some(DrawingAnswer(answerInfo.elements)), false, ans => {})
                        }
                      ),
                      td (
                        s"${gpd.grades.count(_.percentCorrect == 100)} / ${gpd.grades.length}"
                      )
                    )
                  }
                )
              ),
              button ("Done", onClick := (e => setState(state.copy(message = "", mode = InstructorCourseSummaryViewModes.Normal))))
            ) 
        }
    }
  }

  implicit val ec = ExecutionContext.global

  def loadCourseGradeInformation(): Unit = {
    PostFetch.fetch("/getInstructorCourseData", props.course.id,
      (ficd: FullInstructorCourseData) => setState(state.copy(courseGradeInfo = Some(ficd.grades))),
      e => { println(e); setState(state.copy(message = "Error with JSON loading data."))})
  }

  def loadDataAssessment(): Unit = {
    state.selectedAssessment.foreach(sa =>
      PostFetch.fetch("/assessmentGradingData", (props.course.id, sa.assessmentid),
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