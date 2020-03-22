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

// Functions Needed:
// See ist of 
object UserPageMode extends Enumeration {
  val CourseList, Assessment, ChangePassword = Value
}

@react class UserPage extends Component {
  case class Props(userData: UserData)
  case class State(courses: Seq[CourseData], mode: UserPageMode.Value)

  override def componentDidMount() = loadCourses()

  def initialState = State(Nil, UserPageMode.CourseList)

  def render(): ReactElement = {
    state.mode match {
      case UserPageMode.CourseList =>
        div (
          header (h1 ("User page: " + props.userData.username) ),
          button ("Change Password", onClick := (e => setState(state.copy(mode = UserPageMode.ChangePassword)))),
          hr(),
          state.courses.map(cd => div ( cd.name ))// CourseComponent(props.userData, cd).withKey(cd.name))
        )
      case UserPageMode.ChangePassword =>
        ChangePasswordComponent(props.userData, () => setState(state.copy(mode = UserPageMode.CourseList)))
      case UserPageMode.Assessment =>
        div (
          h2 ("Taking Test"),
          button ("Done", onClick := (e => setState(state.copy(mode = UserPageMode.CourseList))))
        )
    }
  }
  
  implicit val ec = ExecutionContext.global

  def loadCourses(): Unit = {
    val headers = new Headers()
    headers.set("Content-Type", "application/json")
    headers.set("Csrf-Token", dom.document.getElementsByTagName("body").apply(0).getAttribute("data-token"))
    Fetch.fetch(
      s"/getCourses?userid=${props.userData.id}",
      RequestInit(method = HttpMethod.POST, mode = RequestMode.cors, headers = headers, body = Json.toJson(props.userData.id).toString())
    ).flatMap { res =>
      res.text()
    }.map { res =>
      Json.fromJson[Seq[CourseData]](Json.parse(res)) match {
        case JsSuccess(cd, path) =>
          println(cd)
          setState(state.copy(courses = cd))
        case e @ JsError(_) =>
          println("Courses error: " + e)
          // setState(_.copy(message = "Error with JSON response from server."))
      }
    }
  }
}