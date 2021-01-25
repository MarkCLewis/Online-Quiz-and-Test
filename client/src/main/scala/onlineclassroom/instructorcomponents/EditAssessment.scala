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

@react class EditAssessment extends Component {
  case class Props(userData: UserData, assessmentData: Option[AssessmentData], allProblems: Seq[ProblemSpec], exitFunc: () => Unit, realoadAssessmentsFunc: () => Unit)
  case class State(message: String, ad: AssessmentData, problemAssocs: Seq[ProblemAssessmentAssociation],
    problemFilter: String, problemCreatorMatch: Boolean)

  def initialState: State = State("", props.assessmentData match {
    case Some(ad) => ad
    case None => AssessmentData(-1, "", "", AutoGradeOptions.Never, props.userData.id)
  }, Nil, "", true)

  override def componentDidMount(): Unit = loadAssociatedProblems()

  def render: ReactElement = {
    val allProbsMap = props.allProblems.map(ps => ps.id -> ps).toMap
    val problemFilterRegex = try { state.problemFilter.r } catch { case ex: Exception => ".*".r }
    div(
      h3 ("Edit Assessment"),
      "Name:",
      input (`type` := "text", value := state.ad.name, onChange := (e => setState(state.copy(ad = state.ad.copy(name = e.target.value))))),
      br(),
      "Description:",
      br(),
      textarea (value := state.ad.description, cols := "80", onChange := (e => setState(state.copy(ad = state.ad.copy(description = e.target.value))))),
      br(),
      "Autograde Setting: ",
      select (
        AutoGradeOptions.asString.zipWithIndex.map { case (opt, i) => option (key := i.toString, value := i.toString, opt) }.toSeq,
        onChange := (e => setState(state.copy(ad = state.ad.copy(autoGrade = e.target.value.toInt))))
      ),
      br(),
      button ("Save", onClick := (e => saveAssessment(true))),
      button ("Save and Exit", onClick := { e => saveAssessment(false); props.exitFunc() }),
      button ("Exit", onClick := (e => props.exitFunc())),
      hr(),
      h3 ("Included Problems:"),
      "Add Problem: ",
      select (
        option (value := "-1", "Select to add"),
        props.allProblems.filter(p => 
                problemFilterRegex.findFirstIn(p.info.name+p.info.prompt).nonEmpty && 
                (!state.problemCreatorMatch || p.creatorid.forall(_ == props.userData.id))).
            zipWithIndex.map { case (pspec, i) => option (key := i.toString, value := pspec.id.toString, pspec.info.name) },
        onChange := (e => if(e.target.value != "-1") updateProblemAssessmentAssoc(ProblemAssessmentAssociation(-1, state.ad.id, e.target.value.toInt, 1.0, false), -1))
      ),
      input (`type` := "text", value := state.problemFilter,
        onChange := (e => setState(state.copy(problemFilter = e.target.value)))),
      ", Only mine:",
      input (`type` := "checkbox", checked := state.problemCreatorMatch,
        onChange := (e => setState(state.copy(problemCreatorMatch = e.target.checked)))),
      table (
        thead ( tr ( th("Name"), th("Weight"), th("Extra Credit"), th())),
        tbody (
          state.problemAssocs.zipWithIndex.map { case (paa, i) =>
            tr ( key := i.toString, 
              td (allProbsMap(paa.problemid).info.name), 
              td ( input (`type` := "number", value := paa.weight.toString, 
                onChange := (e => setState(state.copy(problemAssocs = state.problemAssocs.patch(i, Seq(paa.copy(weight = if (e.target.value.isEmpty) 0.0 else e.target.value.toDouble)), 1)))), 
                onBlur := (e => updateProblemAssessmentAssoc(paa, i))
              )),
              td ( select ( value := paa.extraCredit.toString(),
                option (value := "true", "true"),
                option (value := "false", "false"),
                onChange := (e => updateProblemAssessmentAssoc(paa.copy(extraCredit = e.target.value.toBoolean), i))
              )),
              td (button ("Remove", onClick := (e => removeProblemAssessmentAssoc(i))))
            )
          }
        )
      ),
      br(),
      button ("Exit", onClick := (e => props.exitFunc())),
      state.message
    )
  }

  implicit val ec = ExecutionContext.global

  def loadAssociatedProblems(): Unit = {
    PostFetch.fetch("/loadAssociatedProblems", state.ad.id,
      (paas: Seq[ProblemAssessmentAssociation]) => setState(state.copy(problemAssocs = paas)),
      e => setState(_.copy(message = "Error with Json loading associations.")))
  }

  def saveAssessment(updateState: Boolean): Unit = {
    PostFetch.fetch("/saveAssessment", state.ad,
      (newID: Int) => {
        val saveData = if (state.ad.id < 0) state.ad.copy(id = newID) else state.ad
        if (updateState) setState(state.copy(ad = saveData))
      },
      e => setState(_.copy(message = "Error with Json saving assessment.")))
  }

  def updateProblemAssessmentAssoc(paa: ProblemAssessmentAssociation, index: Int): Unit = {
    PostFetch.fetch("/saveProblemAssessmentAssoc", paa,
      (paaid: Int) => {
        if (paa.id < 0) setState(state.copy(problemAssocs = state.problemAssocs :+ paa.copy(id = paaid))) 
        else setState(state.copy(problemAssocs = state.problemAssocs.patch(index, Seq(paa), 1)))
      },
      e => setState(_.copy(message = "Error with Json saving problem assessment association.")))
  }

  def removeProblemAssessmentAssoc(index: Int): Unit = {
    PostFetch.fetch("/removeProblemAssessmentAssoc", state.problemAssocs(index).id,
      (cnt: Int) => {
        setState(state.copy(problemAssocs = state.problemAssocs.patch(index, Nil, 1)))
      },
      e => setState(_.copy(message = "Error with Json removing problem assessment association.")))
  }
}
