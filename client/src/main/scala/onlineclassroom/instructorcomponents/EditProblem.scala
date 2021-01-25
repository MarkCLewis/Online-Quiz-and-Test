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
  case class Props(userData: UserData, editType: String, problemSpec: Option[ProblemSpec], exitFunc: () => Unit, realoadProblemsFunc: () => Unit)
  case class State(message: String, problemSpec: ProblemSpec)

  def initialState: State = State("", props.problemSpec match {
    case Some(ps) => ps
    case None =>
      props.editType match {
        case "SA" => ProblemSpec(-1, ShortAnswerInfo("", "", Nil), ShortAnswerGradeInfo(0), Some(props.userData.id))
        case "MC" => ProblemSpec(-1, MultipleChoiceInfo("", "", Nil), MultipleChoiceGradeInfo(0), Some(props.userData.id))
        case "Func" => ProblemSpec(-1, WriteFunctionInfo("", "", "", Nil), WriteFunctionGradeInfo("", 10), Some(props.userData.id))
        case "Expr" => ProblemSpec(-1, WriteExpressionInfo("", "", Nil, ""), WriteExpressionGradeInfo("", 10), Some(props.userData.id))
        case "Lambda" => ProblemSpec(-1, WriteLambdaInfo("", "", "", Nil), WriteLambdaGradeInfo("", 10), Some(props.userData.id))
        case "Drawing" => ProblemSpec(-1, DrawingInfo("", "", Nil), DrawingGradeInfo(Nil), Some(props.userData.id))
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
        case info@WriteFunctionInfo(name, prompt, functionName, varSpecs) =>
          val gi = ps.answerInfo.asInstanceOf[WriteFunctionGradeInfo]
          div (
            nameAndPrompt(ps, "Write Function", name, prompt, name => info.copy(name = name), prompt => info.copy(prompt = prompt)),
            "Function Name: ",
            input (value := functionName, 
              onChange := (e => setState(state.copy(problemSpec = ps.copy(info = info.copy(functionName = e.target.value)))))),
            br(),
            "Correct Code: ",
            br(),
            textarea (value := gi.correctCode, rows := "8", cols := "80",
              onChange := (e => setState(state.copy(problemSpec = ps.copy(answerInfo = gi.copy(correctCode = e.target.value)))))), 
            br(),
            "Num Runs: ",
            input (`type` := "number", value := gi.numRuns.toString,
              onChange := (e => setState(state.copy(problemSpec = ps.copy(answerInfo = gi.copy(numRuns = e.target.value.toInt)))))),
            br(),
            varableSpecList(varSpecs, ps, (lst, i) => info.copy(varSpecs = varSpecs.patch(i, lst, 1))),
            AddVaribleSpecComponent(vs => setState(state.copy(problemSpec = ps.copy(info = info.copy(varSpecs = varSpecs :+ vs)))))
          )
        case info@WriteExpressionInfo(name, prompt, varSpecs, generalSetup) =>
          val gi = ps.answerInfo.asInstanceOf[WriteExpressionGradeInfo]
          div (
            nameAndPrompt(ps, "Write Function", name, prompt, name => info.copy(name = name), prompt => info.copy(prompt = prompt)),
            "Correct Code: ",
            br(),
            textarea (value := gi.correctCode, rows := "8", cols := "80",
              onChange := (e => setState(state.copy(problemSpec = ps.copy(answerInfo = gi.copy(correctCode = e.target.value)))))), 
            br(),
            "Num Runs: ",
            input (`type` := "number", value := gi.numRuns.toString,
              onChange := (e => setState(state.copy(problemSpec = ps.copy(answerInfo = gi.copy(numRuns = e.target.value.toInt)))))),
            br(),
            "General Setup: ",
            br(),
            textarea (value := info.generalSetup, rows := "8", cols := "80",
              onChange := (e => setState(state.copy(problemSpec = ps.copy(info = info.copy(generalSetup = e.target.value)))))), 
            br(),
            varableSpecList(varSpecs, ps, (lst, i) => info.copy(varSpecs = varSpecs.patch(i, lst, 1))),
            AddVaribleSpecComponent(vs => setState(state.copy(problemSpec = ps.copy(info = info.copy(varSpecs = varSpecs :+ vs)))))
          )
        case info@WriteLambdaInfo(name, prompt, returnType, varSpecs) =>
          val gi = ps.answerInfo.asInstanceOf[WriteLambdaGradeInfo]
          div (
            nameAndPrompt(ps, "Write Function", name, prompt, name => info.copy(name = name), prompt => info.copy(prompt = prompt)),
            "Return Type: ",
            input (value := returnType, 
              onChange := (e => setState(state.copy(problemSpec = ps.copy(info = info.copy(returnType = e.target.value)))))),
            br(),
            "Correct Code: ",
            br(),
            textarea (value := gi.correctCode, rows := "8", cols := "80",
              onChange := (e => setState(state.copy(problemSpec = ps.copy(answerInfo = gi.copy(correctCode = e.target.value)))))), 
            br(),
            "Num Runs: ",
            input (`type` := "number", value := gi.numRuns.toString,
              onChange := (e => setState(state.copy(problemSpec = ps.copy(answerInfo = gi.copy(numRuns = e.target.value.toInt)))))),
            br(),
            varableSpecList(varSpecs, ps, (lst, i) => info.copy(varSpecs = varSpecs.patch(i, lst, 1))),
            AddVaribleSpecComponent(vs => setState(state.copy(problemSpec = ps.copy(info = info.copy(varSpecs = varSpecs :+ vs)))))
          )
        case info@DrawingInfo(name, prompt, initialElements) =>
          val dgi = state.problemSpec.answerInfo.asInstanceOf[DrawingGradeInfo]
          div (
            nameAndPrompt(ps, "Drawing Edit/Create", name, prompt, name => info.copy(name = name), prompt => info.copy(prompt = prompt)),
            "Initial Elements (Don't add connections.):",
            br(),
            DrawAnswerComponent(true, Nil, initialElements, 800, 400, true, 
              elems => setState(state.copy(problemSpec = ps.copy(info = info.copy(initialElements = elems)))),
              elems => setState(state.copy(problemSpec = ps.copy(info = info.copy(initialElements = elems))))),
            br(),
            "Proper Answer",
            br(),
            DrawAnswerComponent(true, initialElements, dgi.elements, 800, 400, true, 
              elems => setState(state.copy(problemSpec = ps.copy(answerInfo = dgi.copy(elements = elems)))),
              elems => setState(state.copy(problemSpec = ps.copy(answerInfo = dgi.copy(elements = elems))))),
          )
        case ManualEntryInfo(name) => ""
        case ProblemInfoError(name, prompt) => ""
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

  def varableSpecList(varSpecs: Seq[VariableSpec], ps: ProblemSpec, infoSpecPatch: (Seq[VariableSpec], Int) => ProblemInfo): ReactElement =             ol (
    varSpecs.zipWithIndex.map { case (vs, i) =>
      li ( key := s"key$i", 
        vs match {
          case spec: IntSpec => IntSpecEdit(spec, 
            newSpec => setState(state.copy(problemSpec = ps.copy(info = infoSpecPatch(List(newSpec), i)))))
          case spec: DoubleSpec => DoubleSpecEdit(spec, 
            newSpec => setState(state.copy(problemSpec = ps.copy(info = infoSpecPatch(List(newSpec), i)))))
          case spec: StringSpec => StringSpecEdit(spec, 
            newSpec => setState(state.copy(problemSpec = ps.copy(info = infoSpecPatch(List(newSpec), i)))))
          case spec: ListIntSpec => ListIntSpecEdit(spec, 
            newSpec => setState(state.copy(problemSpec = ps.copy(info = infoSpecPatch(List(newSpec), i)))))
          case spec: ArrayIntSpec => ArrayIntSpecEdit(spec, 
            newSpec => setState(state.copy(problemSpec = ps.copy(info = infoSpecPatch(List(newSpec), i)))))
          case spec: ListStringSpec => ListStringSpecEdit(spec, 
            newSpec => setState(state.copy(problemSpec = ps.copy(info = infoSpecPatch(List(newSpec), i)))))
          case spec: ArrayArrayIntSpec => ArrayArrayIntSpecEdit(spec, 
            newSpec => setState(state.copy(problemSpec = ps.copy(info = infoSpecPatch(List(newSpec), i)))))
          case spec: ArrayArrayDoubleSpec => ArrayArrayDoubleSpecEdit(spec, 
            newSpec => setState(state.copy(problemSpec = ps.copy(info = infoSpecPatch(List(newSpec), i)))))
        },
        button ("Remove", onClick := (e => setState(state.copy(problemSpec = ps.copy(info = infoSpecPatch(Nil, i))))))
      )
    }
  )
}