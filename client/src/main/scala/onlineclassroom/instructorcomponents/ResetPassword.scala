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

@react class ResetPassword extends Component {
  case class Props(userData: UserData)
  case class State(username: String, password: String, message: String)

  def initialState: State = State("", "", "")

  def render: ReactElement = div (
    h3("Reset User Password"),
    "Email: ", 
    input(`type` := "text", value := state.username, onChange := (e => setState(state.copy(username = e.target.value)))),
    "Password:", 
    input(`type` := "password", value := state.password, onChange := (e => setState(state.copy(password = e.target.value)))),
    button("Reset", onClick := (event => tryResetPassword())),
    state.message
  )

  implicit val ec = ExecutionContext.global

  def tryResetPassword(): Unit = {
    if (state.username.isEmpty) setState(state.copy(message = "Email is required."))
    else {
      PostFetch.fetch("/resetPassword", NewUserData(state.username, state.password, false),
        (worked: Boolean) => if (worked) {
            setState(State("", "", "User password reset."))
          } else {
            setState(state.copy(message = "Password reset failed."))
          }, 
        e => setState(_.copy(message = "Error with JSON response from server.")))
    }
  }
}