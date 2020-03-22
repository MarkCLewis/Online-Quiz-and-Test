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

@react class CreateUser extends Component {
  case class Props(userData: UserData)
  case class State(username: String, password: String, instructor: Boolean, message: String)

  def initialState: State = State("", "", false, "")

  def render: ReactElement = div (
    h3("Create User"),
    "Email: ", 
    input(`type` := "text", id := "username", value := state.username, onChange := (e => setState(state.copy(username = e.target.value)))),
    "Password:", 
    input(`type` := "password", id := "password", value := state.password, onChange := (e => setState(state.copy(password = e.target.value)))),
    if (props.userData.username == "root") select ( option (value := "false", "Student"), option (value := "true", "Instructor"), 
      onChange := (e => setState(state.copy(instructor = e.target.value == "true")))) else "",
    button("Create", onClick := (event => tryCreateUser())),
    state.message
  )

  implicit val ec = ExecutionContext.global

  def tryCreateUser(): Unit = {
    if (state.username.isEmpty) setState(state.copy(message = "Email is required."))
    else if (state.password.isEmpty) setState(state.copy(message = "Password is required."))
    else {
      PostFetch.fetch("/createUser", NewUserData(state.username, state.password, state.instructor),
        (worked: Boolean) => if (worked) {
            setState(State("", "", false, "User created."))
          } else {
            setState(state.copy(message = "User creation failed."))
          }, 
        e => setState(_.copy(message = "Error with JSON response from server.")))
    }
  }
}