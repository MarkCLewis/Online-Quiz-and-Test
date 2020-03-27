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

/**
  * This component displays a grid of students and their progress through an assessment. This isn't grade information, just
  * whether they have a start record (only for time limited assessments) and whether they have an answer saved for a
  * particular question. By updating this at regular intervals, an instructor can keep track of how students are doing
  * on a particular assessment. It might be nice for this to include the ability to view the current answer to a particular
  * question. The first draft of this might just show the String version on hover without any form of rendering.
  */
@react class MonitorAssessmentComponent extends Component {
  case class Props(userData: UserData, course: CourseData, aci: AssessmentCourseInfo, studentData: Seq[FullStudentData], exitFunc: () => Unit)
  case class State(message: String, data: Option[AssessmentGradingData], starts: Seq[StudentAssessmentStart])

  def initialState: State = State("", None, Nil)

  override def componentDidMount(): Unit = loadData()

  def render: ReactElement = {
    div (
      button ("Exit", onClick := (e => props.exitFunc())),
      state.data match {
        case None => "Loading Data"
        case Some(agd) =>
          val startsMap = state.starts.map(sas => sas.userid -> sas).toMap
          val problemIds = agd.problems.map(_.id)
          val answersByUserMap = agd.problems.flatMap(gpd => gpd.answers.map(ga => (ga.userid, gpd.id) -> ga)).toMap
          table (
            thead (
              tr (
                th ("Student"),
                th ("Start"),
                agd.problems.zipWithIndex.map { case (gpd, i) =>
                  th ( key := i.toString, gpd.spec.info.name)
                }
              )
            ),
            tbody (
              props.studentData.zipWithIndex.map { case (ud, i) => tr (key := i.toString,
                td (ud.email),
                td (startsMap.get(ud.id).map(_.timeStarted).getOrElse(""): String),
                agd.problems.zipWithIndex.map { case (gpd, i) =>
                  td ( 
                    key := i.toString, 
                    answersByUserMap.get(ud.id -> gpd.id).map(_ => "Submitted").getOrElse(""): String,
                    title := answersByUserMap.get(ud.id -> gpd.id).map(_.toString).getOrElse("")
                  )
                }
              )}
            )
          )
      }
    )
  }

  implicit val ec = ExecutionContext.global

  def loadData(): Unit = {
    PostFetch.fetch("/assessmentGradingData", (props.course.id, props.aci.assessmentid),
      (adg: AssessmentGradingData) => setState(state.copy(data = Some(adg))),
      e => setState(_.copy(message = "Error with Json loading assessment grading data.")))
    PostFetch.fetch("/getAssessmentStarts", props.aci.id,
      (starts: Seq[StudentAssessmentStart]) => setState(state.copy(starts = starts)),
      e => setState(_.copy(message = "Error with Json loading student starts.")))
  }
}