package onlineclassroom

import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.html._
import slinky.core.Component
import slinky.core.StatelessComponent

@react class AddVaribleSpecComponent extends Component {
  case class Props(addSpec: VariableSpec => Unit)
  case class State(opt: Int)

  def initialState = State(0)

  def render(): ReactElement = {
    div (
      select (
        option ("Int"),
        option ("Double"),
        option ("String"),
        option ("List[Int]"),
        option ("Array[Int]"),
        option ("List[String]"),
        option ("Array[Array[Int]]"),
        option ("Array[Array[Double]]"),
        onChange := (e => setState(state.copy(opt = e.target.selectedIndex)))
      ),
      button ("Add Variable", onClick := (e => props.addSpec(
        state.opt match {
          case 0 => IntSpec("i", 0, 10)
          case 1 => DoubleSpec("x", 0.0, 10.0)
          case 2 => StringSpec("s", 5, "")
          case 3 => ListIntSpec("lst", 5, 10, 0, 10)
          case 4 => ArrayIntSpec("arr", 5, 10, 0, 10)
          case 5 => ListStringSpec("lst", 5, 10, 5, "")
          case 6 => ArrayArrayIntSpec("mat", 5, 10, 5, 10, 0, 10)
          case 7 => ArrayArrayDoubleSpec("mat", 5, 10, 5, 10, 0.0, 10.0)
        }
      )))
    )
  }
}

@react class IntSpecDisplay extends StatelessComponent {
  case class Props(is: IntSpec)

  def render(): ReactElement = div ( s"Name: ${props.is.name}, Min: ${props.is.min}, Max: ${props.is.max}" )
}

@react class IntSpecEdit extends StatelessComponent {
  case class Props(spec: IntSpec, update: IntSpec => Unit)

  def render(): ReactElement = {
    span (
      strong ("Int -"),
      "Name: ",
      input (value := props.spec.name, placeholder := "Variable name", 
        onChange := (e => props.update(props.spec.copy(name = e.target.value)))),
      "Min: ",
      input (`type` := "number", value := props.spec.min.toString, placeholder := "min value",
        onChange := (e => props.update(props.spec.copy(min = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max: ",
      input (`type` := "number", value := props.spec.max.toString, placeholder := "max value",
        onChange := (e => props.update(props.spec.copy(max = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
    )
  }
}

@react class DoubleSpecEdit extends StatelessComponent {
  case class Props(spec: DoubleSpec, update: DoubleSpec => Unit)

  def render(): ReactElement = {
    span (
      strong ("Double -"),
      "Name: ",
      input (value := props.spec.name, placeholder := "Variable name", 
        onChange := (e => props.update(props.spec.copy(name = e.target.value)))),
      "Min: ",
      input (`type` := "number", value := props.spec.min.toString, placeholder := "min value",
        onChange := (e => props.update(props.spec.copy(min = try { e.target.value.toDouble } catch { case nfe: NumberFormatException => 0 })))),
      "Max: ",
      input (`type` := "number", value := props.spec.max.toString, placeholder := "max value",
        onChange := (e => props.update(props.spec.copy(max = try { e.target.value.toDouble } catch { case nfe: NumberFormatException => 0 })))),
    )
  }
}

@react class StringSpecEdit extends StatelessComponent {
  case class Props(spec: StringSpec, update: StringSpec => Unit)

  def render(): ReactElement = {
    span (
      strong ("String -"),
      "Name: ",
      input (value := props.spec.name, placeholder := "Variable name", 
        onChange := (e => props.update(props.spec.copy(name = e.target.value)))),
      "Length: ",
      input (`type` := "number", value := props.spec.length.toString, placeholder := "length value",
        onChange := (e => props.update(props.spec.copy(length = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Generating Code: ",
      br(),
      textarea (value := props.spec.genCode, placeholder := "Generating code", rows := "5", cols := "80",
        onChange := (e => props.update(props.spec.copy(genCode = e.target.value)))),
    )
  }
}

@react class ListIntSpecEdit extends StatelessComponent {
  case class Props(spec: ListIntSpec, update: ListIntSpec => Unit)

  def render(): ReactElement = {
    span (
      strong ("List[Int] -"),
      "Name: ",
      input (value := props.spec.name, placeholder := "Variable name", 
        onChange := (e => props.update(props.spec.copy(name = e.target.value)))),
      "Min Length: ",
      input (`type` := "number", value := props.spec.minLen.toString, placeholder := "min length value",
        onChange := (e => props.update(props.spec.copy(minLen = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max Length: ",
      input (`type` := "number", value := props.spec.maxLen.toString, placeholder := "max length value",
        onChange := (e => props.update(props.spec.copy(maxLen = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Min: ",
      input (`type` := "number", value := props.spec.min.toString, placeholder := "min value",
        onChange := (e => props.update(props.spec.copy(min = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max: ",
      input (`type` := "number", value := props.spec.max.toString, placeholder := "max value",
        onChange := (e => props.update(props.spec.copy(max = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
    )
  }
}

@react class ArrayIntSpecEdit extends StatelessComponent {
  case class Props(spec: ArrayIntSpec, update: ArrayIntSpec => Unit)

  def render(): ReactElement = {
    span (
      strong ("Array[Int] -"),
      "Name: ",
      input (value := props.spec.name, placeholder := "Variable name", 
        onChange := (e => props.update(props.spec.copy(name = e.target.value)))),
      "Min Length: ",
      input (`type` := "number", value := props.spec.minLen.toString, placeholder := "min length value",
        onChange := (e => props.update(props.spec.copy(minLen = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max Length: ",
      input (`type` := "number", value := props.spec.maxLen.toString, placeholder := "max length value",
        onChange := (e => props.update(props.spec.copy(maxLen = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Min: ",
      input (`type` := "number", value := props.spec.min.toString, placeholder := "min value",
        onChange := (e => props.update(props.spec.copy(min = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max: ",
      input (`type` := "number", value := props.spec.max.toString, placeholder := "max value",
        onChange := (e => props.update(props.spec.copy(max = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
    )
  }
}

@react class ListStringSpecEdit extends StatelessComponent {
  case class Props(spec: ListStringSpec, update: ListStringSpec => Unit)

  def render(): ReactElement = {
    span (
      strong ("List[String] -"),
      "Name: ",
      input (value := props.spec.name, placeholder := "Variable name", 
        onChange := (e => props.update(props.spec.copy(name = e.target.value)))),
      "Min Length: ",
      input (`type` := "number", value := props.spec.minLen.toString, placeholder := "min length value",
        onChange := (e => props.update(props.spec.copy(minLen = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max Length: ",
      input (`type` := "number", value := props.spec.maxLen.toString, placeholder := "max length value",
        onChange := (e => props.update(props.spec.copy(maxLen = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "String Length: ",
      input (`type` := "number", value := props.spec.stringLength.toString, placeholder := "length value",
        onChange := (e => props.update(props.spec.copy(stringLength = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Generating Code: ",
      br(),
      textarea (value := props.spec.genCode, placeholder := "Generating code", rows := "5", cols := "80",
        onChange := (e => props.update(props.spec.copy(genCode = e.target.value)))),
    )
  }
}

@react class ArrayArrayIntSpecEdit extends StatelessComponent {
  case class Props(spec: ArrayArrayIntSpec, update: ArrayArrayIntSpec => Unit)

  def render(): ReactElement = {
    span (
      strong ("Array[Array[[Int]] -"),
      "Name: ",
      input (value := props.spec.name, placeholder := "Variable name", 
        onChange := (e => props.update(props.spec.copy(name = e.target.value)))),
      "Min Length 1: ",
      input (`type` := "number", value := props.spec.minLen1.toString, placeholder := "min length value",
        onChange := (e => props.update(props.spec.copy(minLen1 = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max Length 1: ",
      input (`type` := "number", value := props.spec.maxLen1.toString, placeholder := "max length value",
        onChange := (e => props.update(props.spec.copy(maxLen1 = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Min Length 2: ",
      input (`type` := "number", value := props.spec.minLen2.toString, placeholder := "min length value",
        onChange := (e => props.update(props.spec.copy(minLen2 = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max Length 2: ",
      input (`type` := "number", value := props.spec.maxLen2.toString, placeholder := "max length value",
        onChange := (e => props.update(props.spec.copy(maxLen2 = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Min: ",
      input (`type` := "number", value := props.spec.min.toString, placeholder := "min value",
        onChange := (e => props.update(props.spec.copy(min = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max: ",
      input (`type` := "number", value := props.spec.max.toString, placeholder := "max value",
        onChange := (e => props.update(props.spec.copy(max = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
    )
  }
}

@react class ArrayArrayDoubleSpecEdit extends StatelessComponent {
  case class Props(spec: ArrayArrayDoubleSpec, update: ArrayArrayDoubleSpec => Unit)

  def render(): ReactElement = {
    span (
      strong ("Array[Array[[Double]] -"),
      "Name: ",
      input (value := props.spec.name, placeholder := "Variable name", 
        onChange := (e => props.update(props.spec.copy(name = e.target.value)))),
      "Min Length 1: ",
      input (`type` := "number", value := props.spec.minLen1.toString, placeholder := "min length value",
        onChange := (e => props.update(props.spec.copy(minLen1 = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max Length 1: ",
      input (`type` := "number", value := props.spec.maxLen1.toString, placeholder := "max length value",
        onChange := (e => props.update(props.spec.copy(maxLen1 = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Min Length 2: ",
      input (`type` := "number", value := props.spec.minLen2.toString, placeholder := "min length value",
        onChange := (e => props.update(props.spec.copy(minLen2 = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Max Length 2: ",
      input (`type` := "number", value := props.spec.maxLen2.toString, placeholder := "max length value",
        onChange := (e => props.update(props.spec.copy(maxLen2 = try { e.target.value.toInt } catch { case nfe: NumberFormatException => 0 })))),
      "Min: ",
      input (`type` := "number", value := props.spec.min.toString, placeholder := "min value",
        onChange := (e => props.update(props.spec.copy(min = try { e.target.value.toDouble } catch { case nfe: NumberFormatException => 0 })))),
      "Max: ",
      input (`type` := "number", value := props.spec.max.toString, placeholder := "max value",
        onChange := (e => props.update(props.spec.copy(max = try { e.target.value.toDouble } catch { case nfe: NumberFormatException => 0 })))),
    )
  }
}
