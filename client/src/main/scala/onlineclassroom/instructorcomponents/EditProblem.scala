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

@react class EditProblem extends Component {
  case class Props(editType: String, problemSpec: Option[ProblemSpec], exitFunc: () => Unit, realoadProblemsFunc: () => Unit)
  case class State(message: String, problemSpec: ProblemSpec)

  def initialState: State = State("", props.problemSpec match {
    case Some(ps) => ps
    case None =>
      props.editType match {
        case "SA" => ProblemSpec(-1, ShortAnswerInfo("", "", Nil), ShortAnswerGradeInfo(0))
        case "MC" => ProblemSpec(-1, MultipleChoiceInfo("", "", Nil), MultipleChoiceGradeInfo(0))
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
            nameAndPrompt(ps, "Short Answer Edit/Create", name, prompt, name => info.copy(name = name), prompt => info.copy(prompt = prompt)),
            "Initial Elements (Don't add connections.):",
            br(),
            DrawAnswerComponent(true, Nil, initialElements, 800, 400, true, 
              elems => setState(state.copy(problemSpec = ps.copy(info = info.copy(initialElements = elems)))),
              elems => setState(state.copy(problemSpec = ps.copy(info = info.copy(initialElements = elems)))))
          )
        case info@MultipleChoiceInfo(name, prompt, options) =>
          div (
            nameAndPrompt(ps, "Multiple Choice Edit/Create", name, prompt, name => info.copy(name = name), prompt => info.copy(prompt = prompt)),
            "Options",
            br(),
            options.zipWithIndex.map { case (opt, i) =>
              div (
                key := s"key$i",
                input (`type` := "radio", checked := (state.problemSpec.answerInfo.asInstanceOf[MultipleChoiceGradeInfo].correct == i),
                  onChange := (e => setState(state.copy(problemSpec = ps.copy(answerInfo = MultipleChoiceGradeInfo(i)))))),
                input (`type` := "text", value := opt,
                  onChange := (e => setState(state.copy(problemSpec = ps.copy(info = info.copy(options = options.patch(i, List(e.target.value), 1))))))),
                button (`type` := "button", "Remove",
                  onClick := (e => setState(state.copy(problemSpec = ps.copy(info = info.copy(options = options.patch(i, Nil, 1)))))))
              )
            },
            button (`type` := "button", "Add Option",
              onClick := (e => setState(state.copy(problemSpec = ps.copy(info = info.copy(options = options :+ ""))))))
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

  // TODO: Any connection put in the initial elements will break things. Have to remap the connections for that to work.
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

  def nameAndPrompt(ps: ProblemSpec, header: String, name: String, prompt: String, infoNameSet: String => ProblemInfo, infoPromptSet: String => ProblemInfo): ReactElement = {
    div (
      header,
      br(),
      "Name:",
      input (`type` := "text", value := name, onChange := (e => setState(state.copy(problemSpec = ps.copy(info = infoNameSet(e.target.value)))))),
      br(),
      "Prompt:",
      br(),
      textarea (value := prompt, cols := "100", rows := "8", onChange := (e => setState(state.copy(problemSpec = ps.copy(info = infoPromptSet(e.target.value)))))),
      br(),
      div (dangerouslySetInnerHTML := js.Dynamic.literal(__html = state.problemSpec.info.prompt)),
    )
  }
}