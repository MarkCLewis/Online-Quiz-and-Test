package onlineclassroom

import scalajs.js
import slinky.core.Component
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.html._
import onlineclassroom.ReadsAndWrites._
import slinky.web.svg.set
import org.scalajs.dom.raw.WebSocket
import org.scalajs.dom
import play.api.libs.json.Json
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import scala.scalajs.js.JSON

@react class WriteLambdaQuestion extends Component {
  case class Props(user: UserData, course: CourseData, paaid: Int, info: WriteLambdaInfo, lastAnswer: Option[WriteLambdaAnswer], 
    editable: Boolean, setAnswer: WriteLambdaAnswer => Unit)
  case class State(message: String, answer: Option[WriteLambdaAnswer])

  def initialState: State = State("", props.lastAnswer)

  def render(): ReactElement = {
    div (
      h4 (props.info.name),
      div (dangerouslySetInnerHTML := js.Dynamic.literal(__html = props.info.prompt)),
      br(),
      "Signature: ", "(", props.info.varSpecs.map(vs => s"${vs.name}: ${vs.typeName}").mkString(", "), ") => ", props.info.returnType,
      br(),
      textarea (value := state.answer.map(_.code).getOrElse(""),
        cols := "100", rows := "8",
        disabled := state.answer.map(_.passed).getOrElse(false) || !props.editable,
        onChange := (e => setState(state.copy(answer = Some(WriteLambdaAnswer(e.target.value, state.answer.map(_.passed).getOrElse(false))))))),
      br(),
      button ("Submit", onClick := (e => state.answer.foreach(wfa => if (wfa.code.nonEmpty) submitCode(wfa.code))),
        disabled := state.answer.map(_.passed).getOrElse(false) || !props.editable),
      state.message
    )
  }

  def submitCode(code: String): Unit = {
    state.answer.foreach { ans =>
      if (ans.code.nonEmpty) {
        val sock = new WebSocket(s"ws://${dom.window.location.hostname}:${dom.window.location.port}/submitSocket")
        sock.onmessage = msg => {
          Json.fromJson[CodeSubmitResponse](Json.parse(msg.data.toString)) match {
            case JsSuccess(csr, path) => 
              setState(state.copy(message = csr.message, answer = state.answer.map(_.copy(passed = csr.correct))))
            case e @ JsError(_) => 
              println("Fetch error " + e)
              setState(state.copy(message = "Error parsing server response."))
          }
        }
        sock.onopen = e => sock.send(Json.toJson(SaveAnswerInfo(-1, props.user.id, props.course.id, props.paaid, ans)).toString())
        sock.onerror = e => println("Websocket error: " + JSON.stringify(e))
      } else setState(state.copy(message = "Code can't be empty."))
    }
  }

}