package onlineclassroom

import org.scalajs.dom

import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._

object MainJS {

  def main(args: Array[String]): Unit = {
    println("Call the react stuff.")
    ReactDOM.render(
      div(
        h1("Header"),
        ShortAnswerQuestion(ShortAnswerSpec("What am I thinking now?"), None)
      ),
      dom.document.getElementById("root")
    )

  }
}
