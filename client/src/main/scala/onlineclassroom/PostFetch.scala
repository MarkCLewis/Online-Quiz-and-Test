package onlineclassroom

import org.scalajs.dom
import scala.concurrent.ExecutionContext
import org.scalajs.dom.experimental._
import play.api.libs.json._
import scala.scalajs.js.Thenable.Implicits._

object PostFetch {
  implicit val ec = ExecutionContext.global

  def fetch[A, B](url: String, data: A, success: B => Unit, error: JsError => Unit)(implicit writes: Writes[A], reads: Reads[B]): Unit = {
    val headers = new Headers()
    headers.set("Content-Type", "application/json")
    headers.set("Csrf-Token", dom.document.getElementsByTagName("body").apply(0).getAttribute("data-token"))
    Fetch.fetch(
      url,
      RequestInit(method = HttpMethod.POST, headers = headers, body = Json.toJson(data).toString())
    ).flatMap(_.text()).map { res =>
      Json.fromJson[B](Json.parse(res)) match {
        case JsSuccess(ret, path) => 
          success(ret)
        case e @ JsError(_) => 
          println("Fetch error " + e)
          error(e)
      }
    }
  }
}