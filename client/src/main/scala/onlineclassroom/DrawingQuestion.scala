package onlineclassroom

import scalajs.js
import slinky.core.Component
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.html._
import onlineclassroom.ReadsAndWrites._
import slinky.web.svg.set

@react class DrawingQuestion extends Component {
  case class Props(user: UserData, course: CourseData, paaid: Int, info: DrawingInfo, lastAnswer: Option[DrawingAnswer], 
    editable: Boolean, setAnswer: DrawingAnswer => Unit)
  case class State(message: String, answer: DrawingAnswer, answerid: Option[Int])

  def initialState: State = State("", props.lastAnswer.getOrElse(DrawingAnswer(Nil)), None)

  def render(): ReactElement = {
    div (
      h4 (props.info.name),
      div (dangerouslySetInnerHTML := js.Dynamic.literal(__html = props.info.prompt)),
      br(),
      DrawAnswerComponent(false,props.info.initialElements, state.answer.elements, 800, 400, props.editable, 
        elems => if (props.editable) setState(state.copy(answer = state.answer.copy(elements = elems))),
        elems => if (props.editable) saveAnswer(elems)),
    )
  }

  def saveAnswer(elems: Seq[DrawAnswerElement]): Unit = {
    PostFetch.fetch("/mergeAnswer", SaveAnswerInfo(state.answerid.getOrElse(-1), props.user.id, props.course.id, props.paaid, DrawingAnswer(elems)),
        (answerid: Int) => setState(state.copy(answer = state.answer.copy(elements = elems), answerid = Some(answerid))),
        e => setState(_.copy(message = "Error with JSON response adding answers.")))
  }

}