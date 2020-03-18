package onlineclassroom

import slinky.core.Component
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.html._

@react class ShortAnswerQuestion extends Component {
  case class Props(spec: ShortAnswerSpec, lastAnswer: Option[String])
  case class State(answer: String)

  def initialState: State = State(props.lastAnswer.getOrElse(""))

  def render(): ReactElement = {
    div (
      props.spec.prompt,
      br(),
      textarea(value := state.answer, onChange := (e => setState(state.copy(answer = e.target.value))), cols := "100", rows := "8"),
      br(),
      DrawAnswerComponent(Seq(ReferenceBox(20, 100, "head")), 800, 400)
    )
  }
}