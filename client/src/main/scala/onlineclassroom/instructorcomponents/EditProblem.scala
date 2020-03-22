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

@react class EditProblem extends Component {
  case class Props(editType: String, problemSpec: Option[ProblemSpec], exitFunc: () => Unit, realoadProblemsFunc: () => Unit)
  case class State(message: String, problemSpec: ProblemSpec)

  def initialState: State = State("", props.problemSpec match {
    case Some(ps) => ps
    case None =>
      props.editType match {
        case "SA" => ProblemSpec(-1, ShortAnswerInfo("", "", Nil), ShortAnswerGradeInfo(0))
        // TODO: Need to add in other types of problems for creating
      }
  })

  def render: ReactElement = {
    val ps = state.problemSpec
    div (
      h3 ("Edit Problem"),
      ps.info match {
        case info@ShortAnswerInfo(name, prompt, initialElements) => 
          div (
            "Short Answer Edit/Create",
            br(),
            "Name:",
            input (`type` := "text", value := name, onChange := (e => setState(state.copy(problemSpec = ps.copy(info = info.copy(name = e.target.value)))))),
            br(),
            "Prompt:",
            br(),
            textarea (value := prompt, cols := "100", rows := "8", onChange := (e => setState(state.copy(problemSpec = ps.copy(info = info.copy(prompt = e.target.value)))))),
            br(),
            "Initial Elements:",
            br(),
            DrawAnswerComponent(Nil, initialElements, 800, 400, elems => setState(state.copy(problemSpec = ps.copy(info = info.copy(initialElements = elems)))))
          )
        // TODO: Need to add in other types of problems for editing.
      },
      button ("Save", onClick := (e => saveProblem(true))),
      button ("Save and Exit", onClick := { e => saveProblem(false); props.exitFunc() }),
      button ("Exit", onClick := (e => props.exitFunc())),
      state.message
    )
  }

  implicit val ec = ExecutionContext.global

  def saveProblem(updateState: Boolean): Unit = {
    PostFetch.fetch("/saveProblem", state.problemSpec,
      (newID: Int) => {
        if(updateState) {
          if (state.problemSpec.id < 0) setState(state.copy(message = "Problem Created", problemSpec = state.problemSpec.copy(id = newID)))
          else setState(state.copy(message = "Problem Saved"))
        }
        props.realoadProblemsFunc()
      }, e => setState(state.copy(message = "Error with JSON parsing in save.")))
  }
}