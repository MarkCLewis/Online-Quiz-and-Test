package onlineclassroom

import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import org.scalajs.dom.experimental._
import slinky.core.facade.React

@react class TopComponent extends Component {
  type Props = Unit
  case class State(userData: Option[UserData])

  def initialState = State(None)

  def render(): ReactElement = div ({
    val ret = state.userData.map(ud => 
      (div (
        button ("Logout", onClick := doLogout _),
        br (),
        if (ud.instructor) InstructorPage(ud) else StudentPage(ud)
      )): ReactElement
    ).getOrElse(div(
      LoginPage(ud => doLogin(ud)),
      br(),
      DrawTool(800, 600)
    ):ReactElement)
    ret
  })

  def doLogin(ud: UserData): Unit = {
    setState(State(Some(ud)))
  }

  def doLogout(): Unit = {
    Fetch.fetch("/logout")
    setState(State(None))
  }
}