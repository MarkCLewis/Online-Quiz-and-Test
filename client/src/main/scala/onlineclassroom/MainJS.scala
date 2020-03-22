package onlineclassroom

import org.scalajs.dom

import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._
import org.scalajs.dom.raw.WebSocket

object MainJS {
  def main(args: Array[String]): Unit = {
    println("Call the react stuff.")
    ReactDOM.render(
      div(
        TopComponent(),
//         h1("Samples"),
//         ShortAnswerQuestion(ShortAnswerInfo("Sample", """This is an example quetion for the app I'm writing so you can do quizzes and tests online.
//           There is a text area where you will type most things. The interesting part is the area below it where I have written code
//           that allows you to "draw" things. It has functionality specifically to draw data structures and other elements that are useful
//           on CS quizzes and tests. You pick what functionality you are using by clicking on one of the elements on the top bar. Some things
//           to note are written in the answer box below that would normally display your answer text.""", Seq(ReferenceBox(20, 100, "head"))), Some(
//           ShortAnswerAnswer("""When an element is selected, you can type to add text.
// You can't select or edit the elements that are initially present.
// You can drag elements around.
// Arrow keys move you between editable text with up and down going between the label and the values.
// You can remove the selected element with Ctrl-X.
// When you have an array selected you can add and remove elements at the end with Insert/Ctrl-I and Delete/Ctrl-Z.
// Connections only go from things that could hold references to things that could be referenced.
// Dragging on a connection (either in select or when first placed) will change where it connects to.""", Nil)))
      ),
      dom.document.getElementById("root")
    )

  }
}
