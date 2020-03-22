package onlineclassroom

import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import scala.concurrent.ExecutionContext
import org.scalajs.dom.experimental._
import play.api.libs.json._
import scala.scalajs.js.Thenable.Implicits._
import ReadsAndWrites._

@react class ChangePasswordComponent extends Component {
  case class Props(userData: UserData, exitFunc: () => Unit)
  case class State(oldPassword: String, newPassword: String, message: String)

  def initialState: State = State("", "", "")

  def render: ReactElement = div (
    h2 ("Change Password"),
    div (
      "Old Password: ", input(`type` := "password", id := "oldPassword", value := state.oldPassword, onChange := (e => setState(state.copy(oldPassword = e.target.value))))
    ),
    div (
      "New Password", input(`type` := "password", id := "newPassword", value := state.newPassword, onChange := (e => setState(state.copy(newPassword = e.target.value))))
    ),
    div (
      button("Change", onClick := (event => tryChangePassword())),
      button ("Cancel", onClick := (e => props.exitFunc()))
    ),
    div (
      state.message
    )
  )

  implicit val ec = ExecutionContext.global

  def tryChangePassword(): Unit = {
    println("Try to change password.")
    if (state.oldPassword.isEmpty) setState(state.copy(message = "Old password is required."))
    else if (state.newPassword.isEmpty) setState(state.copy(message = "New password is required."))
    else {
      val headers = new Headers()
      headers.set("Content-Type", "application/json")
      headers.set("Csrf-Token", dom.document.getElementsByTagName("body").apply(0).getAttribute("data-token"))
      Fetch.fetch(
        s"/changePassword",
        RequestInit(method = HttpMethod.POST, mode = RequestMode.cors, headers = headers, body = Json.toJson(PasswordChangeData(props.userData.id, state.oldPassword, state.newPassword)).toString())
      ).flatMap(_.text()).map { res =>
        Json.fromJson[Boolean](Json.parse(res)) match {
          case JsSuccess(worked, path) =>
            if (worked) {
              props.exitFunc()
            } else {
              setState(state.copy(message = "Password change failed."))
            }
          case e @ JsError(_) =>
            setState(_.copy(message = "Error with JSON response from server."))
        }
      }
    }

  }
}