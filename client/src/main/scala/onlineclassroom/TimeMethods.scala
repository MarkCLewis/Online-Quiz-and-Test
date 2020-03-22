package onlineclassroom

import scala.scalajs.js.Date

object TimeMethods {
  def assessmentOpen(aci: AssessmentCourseInfo, studentStart: Option[StudentAssessmentStart], serverTime: Date): Boolean = {
    val startDate = aci.start.map(new Date(_))
    val endDate = aci.end.map(new Date(_))
    val validWithEnd = endDate.map(_.getTime() > serverTime.getTime()).getOrElse(true)
    val validWithStart = startDate.map(_.getTime() < serverTime.getTime()).getOrElse(true)
    val validWithLimit = (for ( start <- studentStart; limit <- aci.timeLimit) yield { serverTime.getTime() < new Date(start.timeStarted).getTime() + limit*60000 }).getOrElse(true)
    validWithStart && validWithEnd && validWithLimit
  }
}