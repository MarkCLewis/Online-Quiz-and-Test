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

@react class WriteExpressionQuestion extends Component {
  case class Props(user: UserData, course: CourseData, paaid: Int, info: WriteExpressionInfo, lastAnswer: Option[WriteExpressionAnswer], 
    editable: Boolean, setAnswer: WriteExpressionAnswer => Unit)
  case class State(message: String, answer: Option[WriteExpressionAnswer], processing: Boolean)

  def initialState: State = State("", props.lastAnswer, false)

  def render(): ReactElement = {
    div (
      h4 (props.info.name),
      div (dangerouslySetInnerHTML := js.Dynamic.literal(__html = props.info.prompt)),
      br(),
      "Variables: ", props.info.varSpecs.map(vs => s"${vs.name}: ${vs.typeName}").mkString(", "),
      br(),
      textarea (value := state.answer.map(_.code).getOrElse(""),
        cols := "100", rows := "8",
        disabled := state.answer.map(_.passed).getOrElse(false) || !props.editable,
        onChange := (e => setState(state.copy(answer = Some(WriteExpressionAnswer(e.target.value, state.answer.map(_.passed).getOrElse(false))))))),
      br(),
      button ("Submit", onClick := (e => state.answer.foreach(wfa => if (wfa.code.nonEmpty) submitCode(wfa.code))),
        disabled := state.answer.map(_.passed).getOrElse(false) || !props.editable || state.processing),
      state.message
    )
  }

  def submitCode(code: String): Unit = {
    state.answer.foreach { ans =>
      if (ans.code.nonEmpty) {
        val protocol = if (dom.window.location.protocol.startsWith("https")) "wss" else "ws"
        val sock = new WebSocket(s"$protocol://${dom.window.location.hostname}:${dom.window.location.port}/submitSocket")
        sock.onmessage = msg => {
          Json.fromJson[CodeSubmitResponse](Json.parse(msg.data.toString)) match {
            case JsSuccess(csr, path) => 
              setState(state.copy(message = csr.message, answer = state.answer.map(_.copy(passed = csr.correct))))
            case e @ JsError(_) => 
              println("Fetch error " + e)
              setState(state.copy(message = "Error parsing server response."))
          }
        }
        sock.onopen = e => {
          setState(state.copy(processing = true))
          sock.send(Json.toJson(SaveAnswerInfo(-1, props.user.id, props.course.id, props.paaid, ans)).toString())
        }
        sock.onerror = e => println("Websocket error: " + JSON.stringify(e))
        sock.onclose = e => setState(state.copy(processing = false))
      } else setState(state.copy(message = "Code can't be empty for a submit."))
    }
  }

}