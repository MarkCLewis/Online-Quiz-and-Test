package onlineclassroom.studentcomponents

import org.scalajs.dom
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import scala.concurrent.ExecutionContext
import org.scalajs.dom.experimental._
import play.api.libs.json._
import scala.scalajs.js.Thenable.Implicits._
import onlineclassroom._
import onlineclassroom.ReadsAndWrites._
import scala.scalajs.js.Date

object CourseViewMode extends Enumeration {
  val Normal, TakeAssessment, ViewAssessment = Value
}

@react class ViewCourse extends Component {
  case class Props(userData: UserData, course: CourseData, exitFunc: () => Unit)
  
  // TODO: Need FullStudentData for this course both for multiplier and for grades.
  case class State(message: String, mode: CourseViewMode.Value, assessments: Seq[AssessmentCourseInfo], starts: Seq[StudentAssessmentStart],
    studentData: Option[FullStudentData], formulas: Seq[GradeFormulaInfo], selectedAssessment: Option[AssessmentCourseInfo], 
    serverTime: Date, multiplier: Double)

  def initialState: State = State("", CourseViewMode.Normal, Nil, Nil, None, Nil, None, new Date, 0.0)

  private var shortInterval: Int = 0;
  private var longInterval: Int = 0;

  implicit val ec = ExecutionContext.global

  override def componentDidMount(): Unit = {
    loadTime()
    loadData()
    shortInterval = dom.window.setInterval(() => updateTimer1Second(), 1000)
    longInterval = dom.window.setInterval(() => loadTime(), 60000)
  }

  override def componentWillUnmount(): Unit = {
    dom.window.clearInterval(shortInterval)
    dom.window.clearInterval(longInterval)
  }

  def render(): ReactElement = {
    val startMap = state.starts.map(s => s.aciid -> s).toMap
    state.mode match {
      case CourseViewMode.Normal =>
        val openAssessments = state.assessments.filterNot(aci => TimeMethods.assessmentClosed(aci, startMap.get(aci.id), state.serverTime, state.multiplier))
        div (
          // TODO: Row tables with grades.
          "All times in the server's timezone.",
          br(),
          "Server Time:",
          state.serverTime.toLocaleString(),
          br(),
          if (!props.course.active) h2 ("This course is no long active. You can't see individual assessments.") else "",
          h3 ("Assessments"),
          "Click an assessment name to start that assessment. You can only reach assessments here when they are open. To see assessments after you are done use the grades table below.",
          br(),
          table (
            thead ( tr ( th ("Name"), th ("Start Time"), th ("End Time"), th ("Length [Minutes]"), th ("Status"))),
            tbody (
              openAssessments.zipWithIndex.map { case (aci, i) =>
                val start = aci.start.getOrElse("None")
                val end = aci.end.getOrElse("None")
                val limit = aci.timeLimit.map(limit => if (state.multiplier != 1.0) s"$limit x ${state.multiplier}" else limit.toString).getOrElse("None")
                tr ( key := i.toString, 
                  td (aci.name, onClick := (e => if (props.course.active && TimeMethods.assessmentOpen(aci, startMap.get(aci.id), state.serverTime, state.multiplier)) setState(state.copy(mode = CourseViewMode.TakeAssessment, selectedAssessment = Some(aci))))), 
                  td (start),
                  td (end),
                  td (limit),
                  td (if (TimeMethods.assessmentOpen(aci, startMap.get(aci.id), state.serverTime, state.multiplier)) "Open" else "Closed") 
                )
              }
            )
          ),
          h3 ("Grades"),
          "Click an assignment name to see individual problems and comments.",
          table (
            thead ( tr (th ("Name"), th ("Group"), th ("Grade"))),
            tbody {
              state.studentData.map { fsd =>
                if (state.assessments.nonEmpty) {
                  val aciByName = state.assessments.map(aci => aci.name -> aci).toMap
                  val groupedAssessments = state.assessments.groupBy(_.group)
                  val groups = groupedAssessments.keys.toSeq.distinct.sortWith((g1, g2) => if (g1.isEmpty || g2.isEmpty) g1 > g2 else g1 < g2)
                  val formulaMap = state.formulas.map(f => f.groupName -> f.formula).toMap
                  val groupRows = groupedAssessments.map { case (group, saci) => group -> (saci.map(_.name).sorted ++ (if (formulaMap.contains(group)) Seq("Total") else Nil))}
                  val rows: Seq[ReactElement] = groups.zipWithIndex.flatMap { case (g, j) => 
                    groupRows(g).zipWithIndex.map { case (rowHead, k) => 
                      tr (key := (j*100+k).toString, 
                        td (rowHead, onClick := (e => if (props.course.active && aciByName.contains(rowHead) && TimeMethods.assessmentViewable(aciByName(rowHead), startMap.get(aciByName(rowHead).id), state.serverTime)) setState(state.copy(mode = CourseViewMode.ViewAssessment, selectedAssessment = aciByName.get(rowHead))))),
                        td (g),
                        td (if (fsd.grades.contains(rowHead)) fsd.grades(rowHead) else Formulas.calcFormula(fsd.grades, formulaMap.get(g).getOrElse(""))))
                    }
                  }
                  rows
                } else Seq(tr ():ReactElement)
              }
            }
          ),
          button ("Exit", onClick := (e => props.exitFunc())),
          state.message
        )
      case CourseViewMode.TakeAssessment =>
        TakeAssessment(props.userData, props.course, state.selectedAssessment.get, state.serverTime, startMap.get(state.selectedAssessment.get.id), 
          () => setState(state.copy(mode = CourseViewMode.Normal)), state.multiplier)
      case CourseViewMode.ViewAssessment =>
        ViewAssessment(props.userData, props.course, state.selectedAssessment.get, () => setState(state.copy(mode = CourseViewMode.Normal)))
    }
  }

  def loadTime(): Unit = {
    PostFetch.fetch("/getServerTime", props.userData.id,
      (time: String) => setState(state.copy(serverTime = new Date(time))),
      e => setState(_.copy(message = "Error with JSON response getting server time.")))
  }

  def loadData(): Unit = {
    PostFetch.fetch("/getCourseAssessments", (props.userData.id, props.course.id),
      (as: Seq[AssessmentCourseInfo]) => setState(state.copy(assessments = as)),
      e => setState(_.copy(message = "Error with JSON response getting assessments.")))
    PostFetch.fetch("/getStudentStarts", (props.userData.id, props.course.id),
      (starts: Seq[StudentAssessmentStart]) => setState(state.copy(starts = starts)),
      e => setState(_.copy(message = "Error with JSON response getting starts.")))
    PostFetch.fetch("/getTimeMultipler", (props.userData.id, props.course.id),
      (mult: Double) => setState(state.copy(multiplier = mult)),
      e => setState(_.copy(message = "Error with JSON response getting multiplier.")))
    PostFetch.fetch("/getFullStudentData", (props.userData.id, props.course.id),
      (fsd: FullStudentData) => setState(state.copy(studentData = Some(fsd))),
      e => setState(_.copy(message = "Error with JSON response getting full student data.")))
    PostFetch.fetch("/getFormulas", props.course.id,
      (gfis: Seq[GradeFormulaInfo]) => setState(state.copy(formulas = gfis)),
      e => setState(_.copy(message = "Error with JSON response getting Formulas.")))
  }

  def updateTimer1Second(): Unit = {
    state.serverTime.setMilliseconds(state.serverTime.getMilliseconds()+1000)
    setState(state.copy(serverTime = state.serverTime))
  }
}
