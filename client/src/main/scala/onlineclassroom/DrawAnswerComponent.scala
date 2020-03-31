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
import slinky.web.html.button
import slinky.web.html.div

@react class DrawTool extends Component {
  case class Props(width: Double, height: Double)
  case class State(elems: Seq[DrawAnswerElement])

  def initialState = State(Nil)

  def render(): ReactElement = DrawAnswerComponent(false, Nil, state.elems, props.width, props.height, true, 
    elems => {println(elems); setState(state.copy(elems = elems))}, elems => {})
}

object Modes extends Enumeration {
  val Select, RefBox, ValBox, DoubleNode, TripleNode, Array, Connector, Curve, Text, Nothing = this.Value
}

@react class DrawAnswerComponent extends Component {
  val componentWidth = 80
  val boxSize = 20
  val curveOffset = 50

  case class Props(startVisible: Boolean, initialElements: Seq[DrawAnswerElement], editableElements: Seq[DrawAnswerElement], width: Double, height: Double, 
    editable: Boolean, setElements: Seq[DrawAnswerElement] => Unit, saveElements: Seq[DrawAnswerElement] => Unit)
  case class State(visible: Boolean, svgElements: Seq[DrawAnswerElement], selected: Int, subselected: Int, mode: Modes.Value, downLoc: Option[(Double, Double)], curLoc: Option[(Double, Double)])
  
  def initialState = State(props.startVisible, props.editableElements, -1, -1, if (props.editable) Modes.Select else Modes.Nothing, None, None)

  override def componentDidMount(): Unit = {
    org.scalajs.dom.window.addEventListener("keydown", keyDownHandler)
  }

  override def componentWillUnmount(): Unit = {
    org.scalajs.dom.window.addEventListener("keydown", keyDownHandler)
  }

  def render(): ReactElement = {
    if (state.visible) {
      div (
        svg(
          width := props.width.toString, 
          height := props.height.toString, 
          stroke := "black", fill := "white",
          svg (
            defs (
              marker (
                id := "arrow", viewBox := "0 0 20 20", refX := 10, refY := 10, markerWidth := 8, markerHeight := 8, orient := "auto",
                path (d := "M 0 0 L 20 10 L 0 20 z")
              )
            ),
            rect (width := props.width.toString, height := props.height.toString),
            props.initialElements.zipWithIndex.map(t => elementToSVG((t._1, t._2 - 1000000), 0)) ++ 
            state.svgElements.zipWithIndex.map(t => elementToSVG(t, props.initialElements.length)) :+
            (if (props.editable) controlElements() else svg( key := "toolbar" ): ReactElement)
          ),
          onMouseDown := (e => mouseDownHandler(e)),
          onMouseMove := (e => mouseMoveHandler(e)),
          onMouseUp := (e => mouseUpHandler(e)),
          onMouseLeave := { e => setState(state.copy(selected = -1)); props.saveElements(state.svgElements) }
        ),
        {
          import slinky.web.html._
          button ("Hide Drawing", onClick := (e => setState(state.copy(visible = false))))
        }
      )
    } else {
      {
        import slinky.web.html._
        div (
          if (state.svgElements.nonEmpty) "Has elements." else "No elements.",
          button ("Show Drawing", onClick := (e => setState(state.copy(visible = true))))
        )
      }
    }    
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
      text (x := 680, y := 30, textAnchor := "middle", "Text", stroke := "black", fill := "black",
        onMouseDown := (e => { e.stopPropagation; setState(state.copy(mode = Modes.Text, selected = -1, subselected = -1)) })),
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
        path (d := s"M $x1 $y1 C $x1a $y1a, $x2a $y2a, $x2 $y2", markerEnd := "url(#arrow)", strokeWidth := "2", fillOpacity := "0.0"),
        stroke := (if (index >= 0 && index == state.selected) "red" else "black"),
        onMouseDown := (e => specialMouseHandler.map(f => f(e)).getOrElse(elementMouseDownHandler(e, ei._1, 0)))
      )
    case (Curve(pnts), index) =>
      svg (
        key := (index + keyOffset).toString,
        polyline (points := pnts.map { case (x, y) => s"$x,$y" }.mkString(" "), fillOpacity := "0.0"),
        stroke := (if (index >= 0 && index == state.selected) "red" else "black"),
        onMouseDown := (e => specialMouseHandler.map(f => f(e)).getOrElse(elementMouseDownHandler(e, ei._1, 0)))
      )
    case (Text(px, py, msg), index) =>
      text (
        key := (index + keyOffset).toString,
        x := px, y := py,
        msg,
        stroke := (if (index >= 0 && index == state.selected) "cyan" else "black"),
        fill := (if (index >= 0 && index == state.selected) "cyan" else "black"),
        onMouseDown := (e => specialMouseHandler.map(f => f(e)).getOrElse(elementMouseDownHandler(e, ei._1, 0)))
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
    if (state.selected >= 0 && state.selected < state.svgElements.length) {
      if (e.ctrlKey && e.key == "x") {
        setState(state.copy(svgElements = removeElement(state.selected), selected = -1))
      } else state.svgElements(state.selected) match {
        case elem@ReferenceBox(px, py, label) => 
          helper(0, label, str => updateElement(state.selected, elem.copy(label = str)))
        case elem@ValueBox(px, py, value, label) =>
          helper(1, if (state.subselected == 0) value else label, str => updateElement(state.selected, if (state.subselected == 0) elem.copy(value = str) else elem.copy(label = str)))
        case elem@DoubleBox(px, py, value, label) => 
          helper(1, if (state.subselected == 0) value else label, str => updateElement(state.selected, if (state.subselected == 0) elem.copy(value = str) else elem.copy(label = str)))
        case elem@TripleBox(px, py, value, label) => 
          helper(1, if (state.subselected == 0) value else label, str => updateElement(state.selected, if (state.subselected == 0) elem.copy(value = str) else elem.copy(label = str)))
        case elem@ArrayOfBoxes(px, py, values, label) => 
          if (e.key == "Insert" || (e.ctrlKey && e.key == "i")) {
            updateElement(state.selected, elem.copy(values = values :+ ""))
          } else if ((e.key == "Delete"  || (e.ctrlKey && e.key == "z")) && values.length > 1) {
            updateElement(state.selected, elem.copy(values = values.dropRight(1)))
          } else {
            helper(values.length, if (state.subselected < 0) label else values(state.subselected), 
              str => updateElement(state.selected, if (state.subselected < 0) elem.copy(label = str) else elem.copy(values = values.patch(state.subselected, Seq(str), 1))))
          }
        case elem@Text(px, py, label) => 
          helper(0, label, str => updateElement(state.selected, elem.copy(msg = str)))
        case _ =>
      }
    }
  }

  def mouseDownHandler(e: SyntheticMouseEvent[Element]): Unit = {
    val x = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetX.asInstanceOf[Double]
    val y = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetY.asInstanceOf[Double]
    state.mode match {
      case Modes.Select =>
        setState(state.copy(selected = -1, subselected = -1))
      case Modes.RefBox =>
        setState(state.copy(svgElements = addElement(ReferenceBox(x, y, "")), selected = 0, subselected = 0))
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
        setState(state.copy(svgElements = addElement(Curve(Seq(x -> y))), selected = 0, subselected = 0, downLoc = Some(x -> y)))
      case Modes.Text =>
        setState(state.copy(svgElements = addElement(Text(x, y, "_")), selected = 0, subselected = 0))
      case Modes.Nothing =>
    }
  }

  def mouseMoveHandler(e: SyntheticMouseEvent[Element]): Unit = {
    state.downLoc.foreach { case (ox, oy) =>
      val x = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetX.asInstanceOf[Double]
      val y = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetY.asInstanceOf[Double]
      if (state.mode == Modes.Select && state.selected >= 0 && state.selected < state.svgElements.length) {
        val dx = x - ox
        val dy = y - oy
        state.svgElements(state.selected) match {
          case elem@ReferenceBox(px, py, label) => 
            updateElement(state.selected, elem.copy(px = elem.px + dx, py = elem.py + dy))
            setState(state.copy(downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case elem@ValueBox(px, py, value, label) =>
            updateElement(state.selected, elem.copy(px = elem.px + dx, py = elem.py + dy))
            setState(state.copy(downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case elem@DoubleBox(px, py, value, label) => 
            updateElement(state.selected, elem.copy(px = elem.px + dx, py = elem.py + dy))
            setState(state.copy(downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case elem@TripleBox(px, py, value, label) => 
            updateElement(state.selected, elem.copy(px = elem.px + dx, py = elem.py + dy))
            setState(state.copy(downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case elem@ArrayOfBoxes(px, py, values, label) => 
            updateElement(state.selected, elem.copy(px = elem.px + dx, py = elem.py + dy))
            setState(state.copy(downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case Connector(e1, sub1, e2) =>
            val closest = findEndConnection(x, y, e1)
            closest.foreach { newE2 => updateElement(state.selected, Connector(e1, sub1, newE2)) }
          case Curve(pnts) =>
            updateElement(state.selected, Curve(pnts.map(p => (p._1+dx, p._2+dy))))
            setState(state.copy(downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case elem@Text(px, py, msg) => 
            updateElement(state.selected, elem.copy(px = elem.px + dx, py = elem.py + dy))
            setState(state.copy(downLoc = Some(x -> y), curLoc = Some(x -> y)))
          case _ =>
        }
      } else if (state.mode == Modes.Connector && state.selected >=0 && state.selected < state.svgElements.length) {
        state.svgElements(state.selected) match {
          case Connector(e1, sub1, e2) =>
            val closest = findEndConnection(x, y, e1)
            closest.foreach { newE2 => updateElement(state.selected, Connector(e1, sub1, newE2))}
          case _ =>
        }
      } else if (state.mode == Modes.Curve && state.selected >=0 && state.selected < state.svgElements.length) {
        state.svgElements(state.selected) match {
          case Curve(pnts) =>
            updateElement(state.selected, Curve((x, y) +: pnts))
          case _ =>
        }
      }

    }
  }

  def mouseUpHandler(e: SyntheticMouseEvent[Element]): Unit = {
    setState(state.copy(downLoc = None, curLoc = None))
  }

  def elementMouseDownHandler(e: SyntheticMouseEvent[Element], elem: DrawAnswerElement, sub: Int): Unit = {
    val x = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetX.asInstanceOf[Double]
    val y = e.nativeEvent.asInstanceOf[scalajs.js.Dynamic].offsetY.asInstanceOf[Double]
    e.stopPropagation()
    if (state.mode == Modes.Select) {
      setState(state.copy(selected = state.svgElements.indexOf(elem), subselected = sub, downLoc = Some(x -> y)))
    } else if (state.mode == Modes.Connector) {
      elem match {
        case _: ReferenceBox | _: DoubleBox | _: TripleBox | _: ArrayOfBoxes =>
          val ostart = elem match {
              case elem@ReferenceBox(px, py, label) => Some(0)
              case elem@DoubleBox(px, py, value, label) => Some(0)
              case elem@TripleBox(px, py, value, label) => Some(if(x < px) 0 else 1)
              case elem@ArrayOfBoxes(px, py, values, label) => Some(((x - (px - boxSize * 0.5 * values.length)) / boxSize).toInt)
              case _ => None
          }
          val e1 = indexForElem(elem)
          if (state.svgElements.collect { case c: Connector => c }.forall(_.e1 != e1)) {
            for (sub1 <- ostart; e2 <- findEndConnection(x, y, indexForElem(elem))) {
              setState(state.copy(svgElements = addElement(Connector(e1, sub1, e2)), selected = state.svgElements.length, subselected = 0, downLoc = Some(x -> y)))  
            }
          }
        case _ =>
      }
    }
  }

  def updateElement(index: Int, newElem: DrawAnswerElement): Unit = {
    val newElems = state.svgElements.patch(state.selected, Seq(newElem), 1)
    props.setElements(newElems)
  }

  def addElement(elem: DrawAnswerElement): Seq[DrawAnswerElement] = {
    val (added, offset) = elem match {
      case Connector(_, _, _) => (state.svgElements :+ elem, 0)
      case _ => (elem +: state.svgElements, 1)
    }
    val newElems = added.map { e => e match {
      case Connector(e1, sub1, e2) => Connector(if (e1 >= 0) e1 + offset else e1, sub1, if (e2 >= 0) e2 + offset else e2)
      case _ => e
    } }
    props.setElements(newElems)
    newElems
  }

  def removeElement(i: Int): Seq[DrawAnswerElement] = {
    val newElems = state.svgElements.zipWithIndex.flatMap { case (e, index) => 
      if (i == index) None else e match {
        case Connector(e1, sub1, e2) => 
          if (e1 == i || e2 == i) None else Some(Connector(if (e1 < i) e1 else e1-1, sub1, if (e2 < i) e2 else e2-1))
        case _ => Some(e)
      } 
    }
    props.setElements(newElems)
    newElems
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
    val e2 = elemByIndex(closest._2)
    e2 match {
      case _: ValueBox | _: DoubleBox | _: TripleBox | _: ArrayOfBoxes => Some(closest._2)
      case _ => None
    }
  }
}

object DrawAnswerComponent {
  override val getDerivedStateFromProps = (nextProps: Props, prevState: State) => {
    prevState.copy(svgElements = nextProps.editableElements)
  }
}