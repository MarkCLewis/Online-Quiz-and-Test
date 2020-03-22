package onlineclassroom

import slinky.core.Component
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.html._
import onlineclassroom.ReadsAndWrites._

@react class ShortAnswerQuestion extends Component {
  case class Props(user: UserData, course: CourseData, paaid: Int, info: ShortAnswerInfo, lastAnswer: Option[ShortAnswerAnswer], 
    editable: Boolean, setAnswer: ShortAnswerAnswer => Unit)
  case class State(message: String, answer: ShortAnswerAnswer, answerid: Option[Int])

  def initialState: State = State("", props.lastAnswer.getOrElse(ShortAnswerAnswer("", Nil)), None)

  def render(): ReactElement = {
    val initialElements = props.info.initialElements ++ (if (props.editable) Nil else state.answer.elements)
    val editableElements = if (props.editable) state.answer.elements else Nil
    div (
      props.info.prompt,
      br(),
      textarea(value := state.answer.text, 
        onChange := (e => if (props.editable) setState(state.copy(answer = state.answer.copy(text = e.target.value)))),
        onBlur := (e => saveText(e.target.value)),
        cols := "100", rows := "8"
      ),
      br(),
      DrawAnswerComponent(props.info.initialElements, props.lastAnswer.map(_.elements).getOrElse(Nil), 800, 400, props.editable, e => { saveElements(e); setState(state.copy(answer = state.answer.copy(elements = e))) }),
      br(),
      state.message
    )
  }

  def saveText(text: String): Unit = {
    mergeAnswer(text, state.answer.elements) 
  }

  def saveElements(elems: Seq[DrawAnswerElement]): Unit = {
    println(s"Saving elements: $elems")
    mergeAnswer(state.answer.text, elems)
  }

  def mergeAnswer(text: String, elems: Seq[DrawAnswerElement]): Unit = {
    PostFetch.fetch("/mergeAnswer", SaveAnswerInfo(state.answerid.getOrElse(-1), props.user.id, props.course.id, props.paaid, state.answer.copy(text = text, elements = elems)),
      (answerid: Int) => setState(state.copy(answerid = Some(answerid))),
      e => setState(_.copy(message = "Error with JSON response merging answers.")))
  }
}