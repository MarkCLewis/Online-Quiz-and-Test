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

@react class GradingInputComponent extends Component {
  case class Props(gd: GradeData, email: String, updateGradeState: GradeData => Unit, updateGradeOnServer: GradeData => Unit)
  case class State(gd: GradeData)

  def initialState: State = State(props.gd)

  def render: ReactElement = {
    div (
      "Percent Correct:",
      input (`type` := "number", value := state.gd.percentCorrect.toString, 
        onChange := { e => 
          val newPercent = if (e.target.value.isEmpty) 0.0 else e.target.value.toDouble
          props.updateGradeState(state.gd.copy(percentCorrect = newPercent))
        },
        onBlur := (e => props.updateGradeOnServer(state.gd))
      ),
      span ("Hover for email", title := props.email),
      br(),
      "Comment:",
      textarea (
        value := state.gd.comments,
        cols := "80",
        onChange := { e => 
          val newComment = e.target.value
          props.updateGradeState(state.gd.copy(comments = newComment))
        },
        onBlur := (e => props.updateGradeOnServer(state.gd))
      )
    )
  }
}

object GradingInputComponent {
  override val getDerivedStateFromProps = (nextProps: Props, prevState: State) => {
    prevState.copy(gd = nextProps.gd)
  }
}