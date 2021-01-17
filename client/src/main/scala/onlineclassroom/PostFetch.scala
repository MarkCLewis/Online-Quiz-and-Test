package onlineclassroom

import org.scalajs.dom
import scala.concurrent.ExecutionContext
import org.scalajs.dom.experimental._
import play.api.libs.json._
import scala.scalajs.js.Thenable.Implicits._
import scala.scalajs.js.JSON

object PostFetch {
  implicit val ec = ExecutionContext.global

  def fetch[A, B](url: String, data: A, success: B => Unit, error: JsError => Unit)(implicit writes: Writes[A], reads: Reads[B]): Unit = {
    val hs = new Headers()
    hs.set("Content-Type", "application/json")
    hs.set("Csrf-Token", dom.document.getElementsByTagName("body").apply(0).getAttribute("data-token"))
    Fetch.fetch(
      url,
      new RequestInit {
        method = HttpMethod.POST
        headers = hs
        body = Json.toJson(data).toString
      }
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