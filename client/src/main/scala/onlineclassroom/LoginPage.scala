package onlineclassroom

import org.scalajs.dom
import slinky.core.annotations.react
import slinky.core.Component
import slinky.core.facade.ReactElement
import slinky.web.html._
import org.scalajs.dom.experimental._

import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits._
import scala.scalajs.js.JSON

import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError

import onlineclassroom._
import onlineclassroom.ReadsAndWrites._

@react class LoginPage extends Component {
  case class Props(doLogin: UserData => Unit)
  case class State(username: Option[String], password: Option[String], message: String)

  def initialState = State(None, None, "")

  def render(): ReactElement = div (
    h1 ("Login"),
    div (
      "Email: ", input(`type` := "text", id := "name", value := state.username.getOrElse(""), onChange := (e => setState(state.copy(username = Some(e.target.value)))))
    ),
    div (
      "Password", input(`type` := "password", id := "password", value := state.password.getOrElse(""), onChange := (e => setState(state.copy(password = Some(e.target.value)))))
    ),
    div (
      button("Login", onClick := (event => tryLogin()))
    ),
    div (
      state.message
    )
  )

  implicit val ec = ExecutionContext.global
  
  def tryLogin(): Unit = {
    if (state.username.isEmpty) setState(state.copy(message = "Email is required."))
    else if (state.password.isEmpty) setState(state.copy(message = "Password is required."))
    else {
      PostFetch.fetch("/tryLogin", LoginData(state.username.get, state.password.get),
        (ud: UserData) => if (ud.id < 0) {
              setState(_.copy(message = "Invalid email or password."))
            } else {
              props.doLogin(ud)
            },
        e => setState(_.copy(message = "Error with JSON response from server.")))
    }
  }
}