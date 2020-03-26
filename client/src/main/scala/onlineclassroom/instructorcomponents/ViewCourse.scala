package onlineclassroom.instructorcomponents

// TODO:
// Instructor Course Display:
// *** Show list of students
// *See grades for students (full table by assessment would be nice)
// Add student
// Adjust time multiplier on the list of students (put text fields in table)
// *** List of assessments
// *** Add an remove assessment
// *** Adjust points, time, autograde, and group on assessment
// Grade an assessment
// Make it so we have groups of assessments each with their own total/average

import scalajs.js
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

object InstructorCourseViewModes extends Enumeration {
  val Normal, Grading = Value
}

@react class ViewCourse extends Component {
  case class Props(userData: UserData, course: CourseData, allAssessments: Seq[AssessmentData], exitFunc: () => Unit)
  case class State(message: String, mode: InstructorCourseViewModes.Value, studentData: Seq[FullStudentData], gradeData: Option[CourseGradeInformation], 
    selectedAssessment: Option[AssessmentCourseInfo], newStudentEmail: String)

  def initialState: State = State("", InstructorCourseViewModes.Normal, Nil, None, None, "")

  override def componentDidMount(): Unit = {
    loadData()
  }

  def render: ReactElement = {
    state.gradeData match {
      case None =>
      div ("Loading Course Data ...", state.message)
      case Some(gd) =>
        state.mode match {
          case InstructorCourseViewModes.Normal =>
            val adMap = props.allAssessments.map(ad => ad.id -> ad).toMap
            val aciByName = gd.assessments.map(aci => aci.name -> aci).toMap
            val groupedAssessments = gd.assessments.groupBy(_.group)
            println(s"groupedAssessments: $groupedAssessments")
            val groups = groupedAssessments.keys.toSeq.sortWith((g1, g2) => if (g1.isEmpty || g2.isEmpty) g1 > g2 else g1 < g2)
            println(s"groups: $groups")
            val formulaMap = gd.formulas.map(f => f.groupName -> f.formula).toMap
            println(s"formulaMap: $formulaMap")
            val groupColumns = groupedAssessments.map { case (group, saci) => group -> (saci.map(_.name).sorted ++ (if (formulaMap.contains(group)) Seq("Total") else Nil))}
            println(s"groupColumns: $groupColumns")
            div (
              h2 (s"${props.course.name}-${props.course.semester}-${props.course.section}", button ("Done", onClick := (e => props.exitFunc()))),
              h3 ("Students and Grades"),
              "Click a column header to grade that assignment.",
              br(),
              table (
                thead (
                  tr ( th ("Email", rowSpan := 2), th ("Time Multiplier", rowSpan := 2), 
                    groups.zipWithIndex.map { case (g, i) => th (key := i.toString, g, colSpan := groupColumns(g).length)}),
                  tr ( groups.zipWithIndex.map { case (g, i) => groupColumns(g).zipWithIndex.map { case (colHead, j) => 
                    th (key := (i*100+j).toString, colHead, onClick := (e => setState(state.copy(mode = InstructorCourseViewModes.Grading, selectedAssessment = aciByName.get(colHead)))))}})
                ),
                tbody (
                  state.studentData.zipWithIndex.map { case (sd, i) =>
                    tr (key := i.toString, 
                      td (sd.email, className := "outlined"), 
                      td (input (`type` := "number", value := sd.timeMultiplier.toString, 
                        onChange := { e => 
                          val newMult = if (e.target.value.isEmpty) 0.0 else e.target.value.toDouble
                          setState(state.copy(studentData = state.studentData.patch(i, Seq(sd.copy(timeMultiplier = newMult)), 1)))
                        },
                        onBlur := (e => updateTimeMultiplier(sd.id, props.course.id, sd.timeMultiplier))
                      )), 
                      groups.zipWithIndex.map { case (g, j) => groupColumns(g).zipWithIndex.map { case (colHead, k) => td (key := (j*100+k).toString, 
                        if (sd.grades.contains(colHead)) sd.grades(colHead) else calcFormula(sd.grades, formulaMap.get(g).getOrElse("")))}})
                  }
                )
              ),
              hr (),
              "Add Student by email:",
              input (`type` := "text", value := state.newStudentEmail, onChange := (e => setState(state.copy(newStudentEmail = e.target.value)))),
              button ("Add", onClick := (e => { addStudent(state.newStudentEmail); setState(state.copy(newStudentEmail = ""))})),
              h3 ("Assessments"),
              "Add Assessment:",
              select (
                option (value := "-1", "Select to add"),
                props.allAssessments.zipWithIndex.map { case (ad, i) => option (key := i.toString, value := ad.id.toString, ad.name) },
                onChange := (e => if(e.target.value != "-1") {
                  val adid = e.target.value.toInt
                  if (adid >= 0) {
                    updateAssessmentCourseAssoc(AssessmentCourseInfo(-1, props.course.id, adid, adMap(adid).name, adMap(adid).description, 100, "", AutoGradeOptions.Never, None, None, None), -1)
                  }
                })
              ),
              br(),
              "Times in yyyy-[m]m-[d]d hh:mm:ss[.f...] format",
              table ( className := "outlined",
                thead (
                  tr (th ("Name"), th("Description"), th("Points"), th("Group"), th("Autograde"), th("Start"), th("End"), th("Minutes"))            
                ),
                tbody ( className := "outlined",
                  state.gradeData.map { gd =>
                    gd.assessments.zipWithIndex.map { case (a, i) => tr ( key := i.toString,
                      td (a.name), td (a.description), 
                      td ( input (`type` := "number", value := a.points.toString, 
                        onChange := (e => setState(state.copy(gradeData = Some(state.gradeData.get.copy(assessments = 
                          state.gradeData.get.assessments.patch(i, Seq(a.copy(points = if (e.target.value.isEmpty()) 0 else e.target.value.toInt)), 1)))))),
                        onBlur := (e => updateAssessmentCourseAssoc(a, i))
                      )), 
                      td (input (`type` := "text", value := a.group,
                        onChange := (e => setState(state.copy(gradeData = Some(state.gradeData.get.copy(assessments = 
                          state.gradeData.get.assessments.patch(i, Seq(a.copy(group = e.target.value)), 1)))))),
                        onBlur := (e => updateAssessmentCourseAssoc(a, i))
                      )), 
                      td ( select ( value := a.autoGrade.toString(),
                        option (value := "0", AutoGradeOptions.asString(0)),
                        option (value := "1", AutoGradeOptions.asString(1)),
                        option (value := "2", AutoGradeOptions.asString(2)),
                        onChange := (e => updateAssessmentCourseAssoc(a.copy(autoGrade = e.target.value.toInt), i))
                      )),
                      td ( input (`type` := "text", value := a.start.getOrElse(""),
                        onChange := (e => setState(state.copy(gradeData = Some(state.gradeData.get.copy(assessments = 
                          state.gradeData.get.assessments.patch(i, Seq(a.copy(start = Some(e.target.value))), 1)))))),
                        onBlur := (e => updateAssessmentCourseAssoc(a.copy(start = Some(e.target.value)), i))
                      )),
                      td ( input (`type` := "text", value := a.end.getOrElse(""),
                        onChange := (e => setState(state.copy(gradeData = Some(state.gradeData.get.copy(assessments = 
                          state.gradeData.get.assessments.patch(i, Seq(a.copy(end = Some(e.target.value))), 1)))))),
                        onBlur := (e => updateAssessmentCourseAssoc(a.copy(end = Some(e.target.value)), i))
                      )), 
                      td ( input (`type` := "text", value := a.timeLimit.map(_.toString).getOrElse(""), 
                        onChange := (e => setState(state.copy(gradeData = Some(state.gradeData.get.copy(assessments = 
                          state.gradeData.get.assessments.patch(i, Seq(a.copy(timeLimit = try { Some(e.target.value.toInt) } catch { case e: NumberFormatException => None })), 1)))))), 
                        onBlur := (e => updateAssessmentCourseAssoc(a, i))
                      ))
                    ) }: ReactElement
                  }.getOrElse(Seq(tr (): ReactElement))
                )
              ),
              state.message,
              button ("Done", onClick := (e => props.exitFunc()))
            )
          case InstructorCourseViewModes.Grading =>
            GradeAssessment(props.userData, props.course, state.selectedAssessment.get, () => setState(state.copy(mode = InstructorCourseViewModes.Normal)))
        }
    }
  }

  implicit val ec = ExecutionContext.global

  def loadData(): Unit = {
    PostFetch.fetch("/getInstructorCourseData", props.course.id,
      (ficd: FullInstructorCourseData) => setState(state.copy(studentData = ficd.students, gradeData = Some(ficd.grades))),
      e => { println(e); setState(state.copy(message = "Error with JSON loading data."))})
  }

  def calcFormula(grades: Map[String, Double], formula: String): Double = {
    0.0 // TODO: make this work.
  }

  def updateAssessmentCourseAssoc(aciUnclean: AssessmentCourseInfo, index: Int): Unit = {
    println(aciUnclean)
    val aci = aciUnclean.copy(start = cleanDateTime(aciUnclean.start), end = cleanDateTime(aciUnclean.end))
    PostFetch.fetch("/saveAssessmentCourseAssoc", aci,
      (aciid: Int) => {
        if (aci.id < 0) setState(state.copy(gradeData = Some(state.gradeData.get.copy(assessments = state.gradeData.get.assessments :+ aci.copy(id = aciid)))))
        else setState(state.copy(gradeData = Some(state.gradeData.get.copy(assessments = state.gradeData.get.assessments.patch(index, Seq(aci), 1)))))
      },
      e => setState(_.copy(message = "Error with Json saving problem assessment association.")))
  }

  def cleanDateTime(ostr: Option[String]): Option[String] = {
    val regex = """\d{4}-\d{1,2}-\d{1,2} \d\d?:\d\d?:\d\d?(\.\d+)?"""
    println(ostr, ostr.map(_.matches(regex)))
    ostr.filter(_.matches(regex))
  }

  def addStudent(email: String): Unit = {
    PostFetch.fetch("/addStudentToCourse", (email, props.course.id),
      (num: Int) => if (num > 0) loadData() else setState(state.copy(message = "Failed to add student.")),
      e => setState(_.copy(message = "Error with Json adding student to course.")))
  }

  def updateTimeMultiplier(userid: Int, courseid: Int, newMult: Double): Unit = {
    PostFetch.fetch("/updateTimeMultiplier", (userid, courseid, newMult),
      (num: Int) => {},
      e => setState(_.copy(message = "Error with Json updating time multiplier.")))
  }
}
