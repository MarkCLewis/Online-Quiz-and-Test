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
  case class State(message: String, prob: Int, gradingData: Option[AssessmentGradingData])

  def initialState: State = State("", 0, None)

  override def componentDidMount(): Unit = loadData()

  def render: ReactElement = {
    div (
      state.gradingData match {
        case None => div ("Loading data...")
        case Some(agd) => div (
          div (
            agd.problems.zipWithIndex.map { case (agd, i) =>
              button ( key := i.toString, s"Problem $i", onClick := (e => setState(state.copy(prob = i))))
            }
          ),
          div (
            div (
              agd.problems(state.prob).spec.info.name,
              br (),
              agd.problems(state.prob).spec.info.prompt
            ),
            div (
              agd.problems(state.prob).answers.zipWithIndex.map { case (ga, pi) =>
                (agd.problems(state.prob).spec.info, ga.answer) match {
                  case (sai: ShortAnswerInfo, saa: ShortAnswerAnswer) => div ( key := pi.toString(),
                    pre (saa.text),
                    br(),
                    DrawAnswerComponent(saa.elements.nonEmpty, sai.initialElements, saa.elements, 800, 400, false, elems => {}, elems => {}),
                    br(),
                    GradingInputComponent(ga.gradeData, (percent, comment) => updateGradeState(percent, comment, ga, agd, pi), gd => updateGradeOnServer(gd, ga, agd, pi)),
                    hr()
                  )
                }
              }
            )
          )
        )
      },
      button ("Exit", onClick := (e => props.exitFunc()))
    )
  }

  implicit val ec = ExecutionContext.global

  def loadData(): Unit = {
    PostFetch.fetch("/assessmentGradingData", (props.course.id, props.aci.assessmentid),
      (adg: AssessmentGradingData) => setState(state.copy(gradingData = Some(adg))),
      e => setState(_.copy(message = "Error with Json loading assessment grading data.")))
  }

  def updateGradeState(percent: Double, comment: String, ga: GradeAnswer, agd: AssessmentGradingData, pi: Int): Unit = {
    val newGA = ga.copy(gradeData = ga.gradeData.map(_.copy(percentCorrect = percent, comments = comment)).orElse(Some(GradeData(-1, ga.id, percent, comment))))
    println(s"newGA = $newGA")
    val newAnswers: Seq[GradeAnswer] = agd.problems(state.prob).answers.patch(pi, Seq(newGA), 1)
    println(s"newAnswers = $newAnswers")
    val newState = state.copy(gradingData = Some(agd.copy(problems = agd.problems.patch(state.prob, Seq(agd.problems(state.prob).copy(answers = newAnswers)), 1))))
    println(s"newState = $newState")
    setState(newState)
  }

  def updateGradeOnServer(ogd: Option[GradeData], ga: GradeAnswer, agd: AssessmentGradingData, pi: Int): Unit = {
    for (gd <- ogd)
      PostFetch.fetch("/setGradeData", gd,
        (newid: Int) => {
          if (gd.id < 0) {
            val newGA = ga.copy(gradeData = Some(gd.copy(id = newid)))
            val newAnswers: Seq[GradeAnswer] = agd.problems(state.prob).answers.patch(pi, Seq(newGA), 1)
            setState(state.copy(gradingData = Some(agd.copy(problems = agd.problems.patch(state.prob, Seq(agd.problems(state.prob).copy(answers = newAnswers)), 1)))))
          }
        },
        e => setState(_.copy(message = "Error with Json updating data.")))
  }
}
