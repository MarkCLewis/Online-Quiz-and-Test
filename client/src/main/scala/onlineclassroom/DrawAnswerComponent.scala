package onlineclassroom

import slinky.core.Component
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
// import slinky.web.html._
import slinky.web.svg._
import scala.scalajs.js.JSON
import slinky.core.TagElement
import slinky.web.SyntheticMouseEvent
import org.scalajs.dom.raw.Element
import slinky.web.SyntheticKeyboardEvent
import slinky.core.facade.React
import slinky.core.SyntheticEvent
import org.scalajs.dom.raw.Event
import org.scalajs.dom.raw.KeyboardEvent
import slinky.web.html.selected

sealed trait DrawAnswerElement
sealed trait ConnectFromElement extends DrawAnswerElement
sealed trait ConnectToElement extends DrawAnswerElement
case class ReferenceBox(px: Double, py: Double, label: String) extends ConnectFromElement
case class ValueBox(px: Double, py: Double, value: String, label: String) extends ConnectToElement
case class DoubleBox(px: Double, py: Double, value: String, label: String) extends ConnectToElement with ConnectFromElement
case class TripleBox(px: Double, py: Double, value: String, label: String) extends ConnectToElement with ConnectFromElement
case class ArrayOfBoxes(px: Double, py: Double, values: Seq[String], label: String) extends ConnectToElement with ConnectFromElement
case class Connector(e1: Int, sub1: Int, e2: Int) extends DrawAnswerElement
case class Curve(pnts: Seq[(Double, Double)]) extends DrawAnswerElement

object Modes extends Enumeration {
  val Select, RefBox, ValBox, DoubleNode, TripleNode, Array, Connector, Curve = this.Value
}

@react class DrawAnswerComponent extends Component {
  val componentWidth = 80
  val boxSize = 20
  val curveOffset = 50

  case class Props(initialElements: Seq[DrawAnswerElement], width: Double, height: Double)
  case class State(svgElements: Seq[DrawAnswerElement], selected: Int, subselected: Int, mode: Modes.Value, downLoc: Option[(Double, Double)], curLoc: Option[(Double, Double)])
  
  def initialState = State(Nil, -1, -1, Modes.Select, None, None)

  override def componentDidMount(): Unit = {
    org.scalajs.dom.window.addEventListener("keydown", keyDownHandler)
  }

  override def componentWillUnmount(): Unit = {
    org.scalajs.dom.window.addEventListener("keydown", keyDownHandler)
  }

  def render(): ReactElement = {
    svg(
      width := props.width.toString, 
      height := props.height.toString, 
      stroke := "black", fill := "white",
      svg (
        defs (
          marker (
            id := "arrow", viewBox := "0 0 10 10", refX := 5, refY := 5, markerWidth := 6, markerHeight := 6, orient := "auto",
            path (d := "M 0 0 L 10 5 L 0 10 z")
          )
        ),
        rect (width := props.width.toString, height := props.height.toString),
        props.initialElements.zipWithIndex.map(t => elementToSVG((t._1, t._2 - 1000000), 0)) ++ 
        state.svgElements.zipWithIndex.map(t => elementToSVG(t, props.initialElements.length)) :+
        controlElements()
      ),
      onMouseDown := (e => mouseDownHandler(e)),
      onMouseMove := (e => mouseMoveHandler(e)),
      onMouseUp := (e => mouseUpHandler(e)),
      onMouseLeave := (e => setState(state.copy(selected = -1)))
    )    
  }

  def controlElements(): ReactElement = {
    val modeInt = state.mode.id
    svg(
      x := 0, y := 0, key := "toolbar",
      rect (x := 0, y := 0, width := (componentWidth * Modes.maxId).toString, height := "50", onMouseDown := (e => e.stopPropagation())),
      rect (x := modeInt * componentWidth, y := 0, width := componentWidth.toString, height := "50", fill := "red"),
      text (key := "-1", x := 40, y := 30, textAnchor := "middle", "Select", fill := "black", onMouseDown := (e => { e.stopPropagation; setState(state.copy(mode = Modes.Select, selected = -1, subselected = -1)) })),
      elementToSVG(ReferenceBox(120, 25, "ref") -> -2, 0, Some(e => { e.stopPropagation; setState(state.copy(mode = Modes.RefBox, selected = -1, subselected = -1)) })),
      elementToSVG(ValueBox(200, 25, "?", "Value") -> -3, 0, Some(e => { e.stopPropagation; setState(state.copy(mode = Modes.ValBox, selected = -1, subselected = -1)) })),
      elementToSVG(DoubleBox(280, 25, "?", "SL") -> -4, 0, Some(e => { e.stopPropagation; setState(state.copy(mode = Modes.DoubleNode, selected = -1, subselected = -1)) })),
      elementToSVG(TripleBox(360, 25, "?", "DL/BST") -> -5, 0, Some(e => { e.stopPropagation; setState(state.copy(mode = Modes.TripleNode, selected = -1, subselected = -1)) })),
      elementToSVG(ArrayOfBoxes(440, 25, Array("?", "?", "?", "..."), "Array") -> -6, 0, Some(e => { e.stopPropagation; setState(state.copy(mode = Modes.Array, selected = -1, subselected = -1)) })),
      svg (
        text (x := 520, y := 20, textAnchor := "middle", "Connect", stroke := "black", fill := "black"),
        line (x1 := 490, y1 := 25, x2 := 550, y2 := 45, markerEnd := "url(#arrow)"),
        onMouseDown := (e => { e.stopPropagation; setState(state.copy(mode = Modes.Connector, selected = -1, subselected = -1)) })
      ),
      svg (
        text (x := 600, y := 20, textAnchor := "middle", "Free", stroke := "black", fill := "black"),
        path (d := "M 570 25 C 630 25, 570 45, 630 45", stroke := "black", fillOpacity := "0.0"),
        onMouseDown := (e => { e.stopPropagation; setState(state.copy(mode = Modes.Curve, selected = -1, subselected = -1)) })
      ),
      onMouseDown := (e => e.stopPropagation())
    )
  }

  def elementToSVG(ei: (DrawAnswerElement, Int), keyOffset: Int, specialMouseHandler: Option[SyntheticMouseEvent[Element] => Unit] = None): ReactElement = ei match {
    case (ReferenceBox(px, py, label), index) =>
      svg (
        key := (index + keyOffset).toString,
        text (x := px, y := py-5, textAnchor := "middle", stroke := (if (state.selected == index) "cyan" else "black"), fill := "black", label),
        rect (x := px-10, y := py, width := boxSize.toString, height := boxSize.toString),
        stroke := (if (index >= 0 && index == state.selected) "red" else "black"),
        onMouseDown := (e => specialMouseHandler.map(f => f(e)).getOrElse(elementMouseDownHandler(e, ei._1, 0)))
      )
    case (ValueBox(px, py, value, label), index) =>
      svg (
        key := (index + keyOffset).toString,
        text (x := px, y := py-5, textAnchor := "middle", stroke := (if (state.selected == index && state.subselected < 0) "cyan" else "black"), fill := "black", label),
        rect (x := px-20, y := py, width := (2*boxSize).toString, height := boxSize.toString),
        text (x := px, y := py+15, textAnchor := "middle", stroke := (if (state.selected == index && state.subselected == 0) "cyan" else "black"), fill := "black", value),
        stroke := (if (index >= 0 && index == state.selected) "red" else "black"),
        onMouseDown := (e => specialMouseHandler.map(f => f(e)).getOrElse(elementMouseDownHandler(e, ei._1, 0)))
      )
    case (DoubleBox(px, py, value, label), index) =>
      svg (
        key := (index + keyOffset).toString,
        text (x := px, y := py-5, textAnchor := "middle", stroke := (if (state.selected == index && state.subselected < 0) "cyan" else "black"), fill := "black", label),
        rect (x := px-20, y := py, width := boxSize.toString, height := boxSize.toString),
        text (x := px-10, y := py+15, textAnchor := "middle", stroke := (if (state.selected == index && state.subselected == 0) "cyan" else "black"), fill := "black", value),
        rect (x := px, y := py, width := boxSize.toString, height := boxSize.toString),
        stroke := (if (index >= 0 && index == state.selected) "red" else "black"),
        onMouseDown := (e => specialMouseHandler.map(f => f(e)).getOrElse(elementMouseDownHandler(e, ei._1, 0)))
      )
    case (TripleBox(px, py, value, label), index) =>
      svg (
        key := (index + keyOffset).toString,
        text (x := px, y := py-5, textAnchor := "middle", stroke := (if (state.selected == index && state.subselected < 0) "cyan" else "black"), fill := "black", label),
        rect (x := px-30, y := py, width := boxSize.toString, height := boxSize.toString),
        rect (x := px-10, y := py, width := boxSize.toString, height := boxSize.toString),
        text (x := px, y := py+15, textAnchor := "middle", stroke := (if (state.selected == index && state.subselected == 0) "cyan" else "black"), fill := "black", value),
        rect (x := px+10, y := py, width := boxSize.toString, height := boxSize.toString),
        stroke := (if (index >= 0 && index == state.selected) "red" else "black"),
        onMouseDown := (e => specialMouseHandler.map(f => f(e)).getOrElse(elementMouseDownHandler(e, ei._1, 0)))
      )
    case (ArrayOfBoxes(px, py, values, label), index) =>
      svg (
        key := (index + keyOffset).toString,
        text (x := px, y := py-5, textAnchor := "middle", stroke := (if (state.selected == index && state.subselected < 0) "cyan" else "black"), fill := "black", label),
        values.zipWithIndex.map { case (v, i) =>
          svg ( key := i.toString,
            rect (x := boxSize * i + px - boxSize * 0.5 * values.length, y := py, width := boxSize.toString, height := boxSize.toString),
            text (x := boxSize * i + px - boxSize * 0.5 * values.length + 0.5 * boxSize, y := py+15, textAnchor := "middle", stroke := (if (state.selected == index && state.subselected == i) "cyan" else "black"), fill := "black", v)
          )
        },
        stroke := (if (index >= 0 && index == state.selected) "red" else "black"),
        onMouseDown := (e => specialMouseHandler.map(f => f(e)).getOrElse(elementMouseDownHandler(e, ei._1, 0)))
      )
    case (Connector(e1, sub1, e2), index) =>
      val (x1, y1, x1a, y1a) = elemByIndex(e1) match {
        case elem@ReferenceBox(px, py, label) => (px, py + boxSize*0.5, px + curveOffset, py + boxSize*0.5)
        case elem@DoubleBox(px, py, value, label) => (px + boxSize * 0.5, py + boxSize*0.5, px + boxSize*0.5 + curveOffset, py + boxSize*0.5)
        case elem@TripleBox(px, py, value, label) => 
          if (sub1 == 0) (px - boxSize, py + boxSize*0.5, px - boxSize - curveOffset, py + boxSize*0.5 + 0.5 * curveOffset)
          else (px + boxSize, py + boxSize*0.5, px + boxSize + curveOffset, py + boxSize*0.5 - 0.5 * curveOffset)
        case elem@ArrayOfBoxes(px, py, values, label) => 
          val sx = px - 0.5*boxSize*values.length + 0.5*boxSize + boxSize * sub1
          (sx, py + boxSize*0.5, sx, py + boxSize*0.5 + curveOffset)
        case _ => (0.0, 0.0, 0.0, 0.0)
      }
      def endHelper(px: Double, py: Double, sizeX: Double, sizeY: Double): (Double, Double, Double, Double) = {
        val sgnX = (x1 - px).signum
        val sgnY = (y1 - py).signum
        val (ex, ey) = (px + sgnX * sizeX, py + sgnY * sizeY)
        (ex + sgnX * curveOffset, ey + sgnY * curveOffset, ex, ey)
      }
      val (x2a, y2a, x2, y2) = elemByIndex(e2) match {
        case elem@ValueBox(px, py, value, label) => endHelper(px, py + boxSize * 0.5, boxSize, boxSize*0.5)
        case elem@DoubleBox(px, py, value, label) => endHelper(px, py + boxSize * 0.5, boxSize, boxSize*0.5)
        case elem@TripleBox(px, py, value, label) => endHelper(px, py + boxSize * 0.5, boxSize*1.5, boxSize*0.5)
        case elem@ArrayOfBoxes(px, py, values, label) => endHelper(px, py + boxSize * 0.5, 0.5 * boxSize * values.length, boxSize*0.5)
        case elem => 
          println(s"Connector bad match for $e2, $elem")
          (0.0, 0.0, 0.0, 0.0)
      }
      svg (
        key := (index + keyOffset).toString,
        path (d := s"M $x1 $y1 C $x1a $y1a, $x2a $y2a, $x2 $y2", markerEnd := "url(#arrow)", fillOpacity := "0.0"),
        stroke := (if (index >= 0 && index == state.selected) "red" else "black"),
        onMouseDown := (e => specialMouseHandler.map(f => f(e)).getOrElse(elementMouseDownHandler(e, ei._1, 0)))
      )
    case (Curve(pnts), index) =>
      svg (
        key := (index + keyOffset).toString

      )
  }

  def keyDownHandler(e: KeyboardEvent): Unit = {
    def helper(numBottom: Int, os: => String, setStr: String => Unit): Unit = {
      if (e.key == "ArrowRight" && numBottom != 0 && state.subselected >= 0) setState(state.copy(subselected = (state.subselected + 1) % numBottom))
      else if(e.key == "ArrowLeft" && numBottom != 0 && state.subselected >= 0) setState(state.copy(subselected = (state.subselected + numBottom - 1) % numBottom))
      else if(e.key == "ArrowUp" && numBottom != 0) setState(state.copy(subselected = -1))
      else if(e.key == "ArrowDown" && numBottom != 0) setState(state.copy(subselected = 0))
      else {
        val oldStr = os
        val newStr = if (e.key == "Backspace") { oldStr.dropRight(1) } else if (e.key.length == 1) { oldStr + e.key } else oldStr
        setStr(newStr)
      }
    }
    println(e.key, e.ctrlKey, state.selected, state.subselected, state.mode)
    if (state.selected >= 0 && state.selected < state.svgElements.length) {
      if (e.ctrlKey && e.key == "x") {
        setState(state.copy(svgElements = removeElement(state.selected), selected = -1))
      } else state.svgElements(state.selected) match {
        case elem@ReferenceBox(px, py, label) => 
          helper(0, label, str => setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(elem.copy(label = str)), 1))))
        case elem@ValueBox(px, py, value, label) =>
          helper(1, if (state.subselected == 0) value else label, str => setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(if (state.subselected == 0) elem.copy(value = str) else elem.copy(label = str)), 1))))
        case elem@DoubleBox(px, py, value, label) => 
          helper(1, if (state.subselected == 0) value else label, str => setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(if (state.subselected == 0) elem.copy(value = str) else elem.copy(label = str)), 1))))
        case elem@TripleBox(px, py, value, label) => 
          helper(1, if (state.subselected == 0) value else label, str => setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(if (state.subselected == 0) elem.copy(value = str) else elem.copy(label = str)), 1))))
        case elem@ArrayOfBoxes(px, py, values, label) => 
          if (e.key == "Insert") {
            setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(elem.copy(values = values :+ "")), 1)))
          } else if (e.key == "Delete" && values.length > 1) {
            setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(elem.copy(values = values.dropRight(1))), 1)))
          } else {
            helper(values.length, if (state.subselected < 0) label else values(state.subselected), 
              str => setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(if (state.subselected < 0) elem.copy(label = str) else elem.copy(values = values.patch(state.subselected, Seq(str), 1))), 1))))
          }
        case _ =>
      }
    }
  }

  def mouseDownHandler(e: SyntheticMouseEvent[Element]): Unit = {
    println("Main down handler")
    val x = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetX.asInstanceOf[Double]
    val y = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetY.asInstanceOf[Double]
    state.mode match {
      case Modes.Select =>
        setState(state.copy(selected = -1, subselected = -1))
      case Modes.RefBox =>
        setState(state.copy(svgElements = addElement(ReferenceBox(x, y, "ref")), selected = 0, subselected = 0))
      case Modes.ValBox =>
        setState(state.copy(svgElements = addElement(ValueBox(x, y, "", "")), selected = 0, subselected = 0))
      case Modes.DoubleNode =>
        setState(state.copy(svgElements = addElement(DoubleBox(x, y, "", "")), selected = 0, subselected = 0))
      case Modes.TripleNode =>
        setState(state.copy(svgElements = addElement(TripleBox(x, y, "", "")), selected = 0, subselected = 0))
      case Modes.Array =>
        setState(state.copy(svgElements = addElement(ArrayOfBoxes(x, y, Seq.fill(8)(""), "")), selected = 0, subselected = 0))
      case Modes.Connector =>
        // Do nothing. Can't start a connector in open space.
      case Modes.Curve =>
        // TODO:
    }
  }

  def mouseMoveHandler(e: SyntheticMouseEvent[Element]): Unit = {
    println(s"m1 ${state.downLoc}")
    state.downLoc.foreach { case (ox, oy) =>
      println("Move handler")
      val x = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetX.asInstanceOf[Double]
      val y = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetY.asInstanceOf[Double]
      if (state.mode == Modes.Select && state.selected >= 0 && state.selected < state.svgElements.length) {
        val dx = x - ox
        val dy = y - oy
        state.svgElements(state.selected) match {
          case elem@ReferenceBox(px, py, label) => 
            setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(elem.copy(px = elem.px + dx, py = elem.py + dy)), 1), downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case elem@ValueBox(px, py, value, label) =>
            setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(elem.copy(px = elem.px + dx, py = elem.py + dy)), 1), downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case elem@DoubleBox(px, py, value, label) => 
            setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(elem.copy(px = elem.px + dx, py = elem.py + dy)), 1), downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case elem@TripleBox(px, py, value, label) => 
            setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(elem.copy(px = elem.px + dx, py = elem.py + dy)), 1), downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case elem@ArrayOfBoxes(px, py, values, label) => 
            setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(elem.copy(px = elem.px + dx, py = elem.py + dy)), 1), downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case Connector(e1, sub1, e2) =>
            val closest = findEndConnection(x, y, e1)
            closest.foreach { newE2 => setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(Connector(e1, sub1, newE2)), 1)))}
          case _ =>
        }
      } else if (state.mode == Modes.Connector && state.selected >=0 && state.selected < state.svgElements.length) {
        state.svgElements(state.selected) match {
          case Connector(e1, sub1, e2) =>
            val closest = findEndConnection(x, y, e1)
            closest.foreach { newE2 => setState(state.copy(svgElements = state.svgElements.patch(state.selected, Seq(Connector(e1, sub1, newE2)), 1)))}
          case _ =>
        }
      } else if (state.mode == Modes.Curve) {
        // TODO:
      }

    }
  }

  def mouseUpHandler(e: SyntheticMouseEvent[Element]): Unit = {
    // TODO:
    setState(state.copy(downLoc = None, curLoc = None))
  }

  def elementMouseDownHandler(e: SyntheticMouseEvent[Element], elem: DrawAnswerElement, sub: Int): Unit = {
    println("Element mouse down")
    val x = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetX.asInstanceOf[Double]
    val y = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetY.asInstanceOf[Double]
    e.stopPropagation()
    if (state.mode == Modes.Select) {
      setState(state.copy(selected = state.svgElements.indexOf(elem), subselected = sub, downLoc = Some(x -> y)))
    } else if (state.mode == Modes.Connector) {
      elem match {
        case cf: ConnectFromElement =>
          val ostart = cf match {
              case elem@ReferenceBox(px, py, label) => Some(0)
              case elem@DoubleBox(px, py, value, label) => Some(0)
              case elem@TripleBox(px, py, value, label) => Some(if(x < px) 0 else 1)
              case elem@ArrayOfBoxes(px, py, values, label) => Some(((x - (px - boxSize * 0.5 * values.length)) / boxSize).toInt)
          }
          for (sub1 <- ostart; e2 <- findEndConnection(x, y, indexForElem(elem))) {
            setState(state.copy(svgElements = addElement(Connector(indexForElem(cf), sub1, e2)), selected = state.svgElements.length, subselected = 0, downLoc = Some(x -> y)))  
          }
        case _ =>
      }
    }
  }

  def addElement(elem: DrawAnswerElement): Seq[DrawAnswerElement] = {
    val (added, offset) = elem match {
      case Connector(_, _, _) => (state.svgElements :+ elem, 0)
      case _ => (elem +: state.svgElements, 1)
    }
    added.map { e => e match {
      case Connector(e1, sub1, e2) => Connector(if (e1 >= 0) e1 + offset else e1, sub1, if (e2 >= 0) e2 + offset else e2)
      case _ => e
    } }
  }

  def removeElement(i: Int): Seq[DrawAnswerElement] = {
    state.svgElements.zipWithIndex.flatMap { case (e, index) => 
      if (i == index) None else e match {
        case Connector(e1, sub1, e2) => 
          if (e1 == i || e2 == i) None else Some(Connector(if (e1 < i) e1 else e1-1, sub1, if (e2 < i) e2 else e2-1))
        case _ => Some(e)
      } 
    }
  }

  def indexForElem(elem: DrawAnswerElement): Int = {
    val si = state.svgElements.indexOf(elem)
    if (si >= 0) si else -props.initialElements.indexOf(elem)-1
  }

  def elemByIndex(index: Int): DrawAnswerElement = {
    if (index >= 0) state.svgElements(index) else props.initialElements(-index - 1)
  }

  def findEndConnection(x: Double, y: Double, e1: Int): Option[Int] = {
    def distToSqr(x2: Double, y2: Double): Double = (x-x2)*(x-x2) + (y-y2)*(y-y2)
    val closest = (state.svgElements.zipWithIndex ++ props.initialElements.zipWithIndex.map(t => t._1 -> (-t._2 - 1))).minBy { case (elem, i) => 
      elem match {
        case elem@ValueBox(px, py, value, label) => distToSqr(px, py)
        case elem@DoubleBox(px, py, value, label) => distToSqr(px, py)
        case elem@TripleBox(px, py, value, label) => distToSqr(px, py)
        case elem@ArrayOfBoxes(px, py, values, label) => distToSqr(px, py)
        case _ => 1e100
      }
    }
    Some(closest._2)
  }
}