package onlineclassroom

import slinky.core.Component
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.html._

@react class ShortAnswerQuestion extends Component {
  case class Props(info: ShortAnswerInfo, lastAnswer: Option[ShortAnswerAnswer])
  case class State(answer: ShortAnswerAnswer)

  def initialState: State = State(props.lastAnswer.getOrElse(ShortAnswerAnswer("", Nil)))

  def render(): ReactElement = {
    div (
      props.info.prompt,
      br(),
      textarea(value := state.answer.text, onChange := (e => setState(state.copy(answer = state.answer.copy(text = e.target.value)))), cols := "100", rows := "8"),
      br(),
      DrawAnswerComponent(props.info.initialElements, Nil, 800, 400, e => setState(state.copy(answer = state.answer.copy(elements = e))))
    )
  }
}