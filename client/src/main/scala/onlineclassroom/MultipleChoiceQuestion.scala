package onlineclassroom

import scalajs.js
import slinky.core.Component
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.html._
import onlineclassroom.ReadsAndWrites._
import slinky.web.svg.set

@react class MultipleChoiceQuestion extends Component {
  case class Props(user: UserData, course: CourseData, paaid: Int, info: MultipleChoiceInfo, lastAnswer: Option[MultipleChoiceAnswer], 
    editable: Boolean, setAnswer: MultipleChoiceAnswer => Unit)
  case class State(message: String, answer: Option[MultipleChoiceAnswer])

  def initialState: State = State("", props.lastAnswer)

  def render(): ReactElement = {
    div (
      h4 (props.info.name),
      div (dangerouslySetInnerHTML := js.Dynamic.literal(__html = props.info.prompt)),
      br(),
      props.info.options.zipWithIndex.map { case (opt, i) =>
        div ( key := s"key$i",
          label (
            input (`type` := "radio", value := opt, disabled := !props.editable,
              checked := state.answer.exists(_.answer == i),
              onChange := (e => submitAnswer(i))
            ),
            opt
          )
        )
      }
    )
  }

  def submitAnswer(id: Int): Unit = {
    PostFetch.fetch("/addAnswer", SaveAnswerInfo(-1, props.user.id, props.course.id, props.paaid, MultipleChoiceAnswer(id)),
        (answerid: Int) => setState(state.copy(answer = Some(MultipleChoiceAnswer(id)))),
        e => setState(_.copy(message = "Error with JSON response adding answers.")))
  }

}