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
  case class Props(gd: Option[GradeData], updateGradeState: (Double, String) => Unit, updateGradeOnServer: Option[GradeData] => Unit)
  case class State(gd: Option[GradeData])

  def initialState: State = State(props.gd)

  def render: ReactElement = {
    println(state.gd)
    div (
      "Percent Correct:",
      input (`type` := "number", value := state.gd.map(_.percentCorrect.toString).getOrElse(""), 
        onChange := { e => 
          val newPercent = if (e.target.value.isEmpty) 0.0 else e.target.value.toDouble
          props.updateGradeState(newPercent, state.gd.map(_.comments).getOrElse(""))
        },
        onBlur := (e => props.updateGradeOnServer(state.gd))
      ),
      br(),
      "Comment:",
      textarea (
        value := state.gd.map(_.comments).getOrElse(""),
        onChange := { e => 
          val newComment = e.target.value
          println("Updating comment to " + newComment)
          props.updateGradeState(state.gd.map(_.percentCorrect).getOrElse(0), newComment)
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