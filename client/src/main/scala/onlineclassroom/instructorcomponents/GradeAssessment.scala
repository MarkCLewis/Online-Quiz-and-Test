package onlineclassroom.instructorcomponents

import scalajs.js
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

  override def componentDidMount(): Unit = {
    loadData()
  }

  def render: ReactElement = {
    div (
      state.gradingData match {
        case None => div ("Loading data...")
        case Some(agd) => div (
          div (
            agd.problems.zipWithIndex.map { case (agd, i) =>
              button ( key := i.toString, s"Problem ${i+1}", onClick := (e => setState(state.copy(prob = i))))
            }
          ),
          div (
            div (
              agd.problems(state.prob).spec.info.name,
              br (),
              span (dangerouslySetInnerHTML := js.Dynamic.literal(__html = agd.problems(state.prob).spec.info.prompt)),
              hr ()
            ),
            div {
              val gpd = agd.problems(state.prob)
              val answerMap = gpd.answers.map(ga => ga.userid -> ga).toMap
              val gradeMap = gpd.grades.map(gd => gd.userid -> gd).toMap
              agd.students.zipWithIndex.map { case (student, i) => 
                gpd.spec.info match {
                  case sai: ShortAnswerInfo => div ( key := i.toString(),
                    answerMap.get(student.id) match {
                      case Some(ga) =>
                        ga.answer match {
                          case saa: ShortAnswerAnswer => div (
                            textarea (value := saa.text, cols := "100", rows := "8", onChange := (e => {})),
                            br(),
                            DrawAnswerComponent(saa.elements.nonEmpty, sai.initialElements, saa.elements, 800, 400, false, elems => {}, elems => {}),
                          )
                          case _ => "Answer type mismatch"
                        }
                      case None => div (s"No answer for ${student.username}")
                    },
                    br(),
                    GradingInputComponent(gradeMap.get(student.id).getOrElse(GradeData(-1, student.id, props.course.id, gpd.paaid, 0.0, "")), 
                      student.username,
                      gd => updateGradeState(gd, agd), 
                      gd => updateGradeOnServer(gd, agd)),
                    hr()
                  )
                  case mci: MultipleChoiceInfo => div ( key := i.toString(),
                    answerMap.get(student.id) match {
                      case Some(ga) =>
                        ga.answer match {
                          case mca: MultipleChoiceAnswer => div (
                            s"${mca.answer} - ${mci.options(mca.answer)}",
                          )
                          case _ => "Answer type mismatch"
                        }
                      case None => div (s"No answer for ${student.username}")
                    },
                    br(),
                    GradingInputComponent(gradeMap.get(student.id).getOrElse(GradeData(-1, student.id, props.course.id, gpd.paaid, 0.0, "")), 
                      student.username,
                      gd => updateGradeState(gd, agd), 
                      gd => updateGradeOnServer(gd, agd)),
                    hr()
                  )
                }
              }
            }
          ),
          div (
            agd.problems.zipWithIndex.map { case (agd, i) =>
              button ( key := i.toString, s"Problem ${i+1}", onClick := (e => setState(state.copy(prob = i))))
            }
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

  def updateGradeState(gd: GradeData, agd: AssessmentGradingData): Unit = {
    println(s"updating grade $gd")
    val gradeIndex = agd.problems(state.prob).grades.indexWhere(_.userid == gd.userid)
    println(s"Index is $gradeIndex")
    val newGrades = if (gradeIndex < 0) {
      agd.problems(state.prob).grades :+ gd
    } else {
      agd.problems(state.prob).grades.patch(gradeIndex, Seq(gd), 1)
    }
    println(s"newGrades = $newGrades")
    val newState = state.copy(gradingData = Some(agd.copy(problems = agd.problems.patch(state.prob, Seq(agd.problems(state.prob).copy(grades = newGrades)), 1))))
    setState(newState)
  }

  def updateGradeOnServer(gd: GradeData, agd: AssessmentGradingData): Unit = {
    PostFetch.fetch("/setGradeData", gd,
      (newid: Int) => {
        if (gd.id < 0) {
          val newGD = gd.copy(id = newid)
          val gradeIndex = agd.problems(state.prob).grades.indexWhere(_.userid == gd.userid)
          val newGrades = if (gradeIndex < 0) {
            agd.problems(state.prob).grades :+ newGD
          } else {
            agd.problems(state.prob).grades.patch(gradeIndex, Seq(newGD), 1)
          }
          val newState = state.copy(gradingData = Some(agd.copy(problems = agd.problems.patch(state.prob, Seq(agd.problems(state.prob).copy(grades = newGrades)), 1))))
          setState(newState)
        }
      },
      e => setState(_.copy(message = "Error with Json updating data.")))
  }
}
