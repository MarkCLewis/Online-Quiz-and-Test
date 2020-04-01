package onlineclassroom

import scala.scalajs.js.Date

object TimeMethods {
  def assessmentOpen(aci: AssessmentCourseInfo, studentStart: Option[StudentAssessmentStart], serverTime: Date, multiplier: Double): Boolean = {
    val startDate = aci.start.map(new Date(_))
    val endDate = aci.end.map(new Date(_))
    val validWithEnd = endDate.map(_.getTime() > serverTime.getTime()).getOrElse(true)
    val validWithStart = startDate.map(_.getTime() < serverTime.getTime()).getOrElse(true)
    val validWithLimit = (for ( start <- studentStart; limit <- aci.timeLimit) yield { 
      serverTime.getTime() < new Date(start.timeStarted).getTime() + limit * multiplier * 60000 
    }).getOrElse(true)
    validWithStart && validWithEnd && validWithLimit
  }

  /**
    * Returns the miliseconds until this assessment closes.
    *
    * @param aci
    * @param studentStart
    * @param serverTime
    * @return
    */
  def timeRemaining(aci: AssessmentCourseInfo, studentStart: Option[StudentAssessmentStart], serverTime: Date, multiplier: Double): Double = {
    val endLeft = aci.end.map(e => new Date(e).getTime() - serverTime.getTime()).getOrElse(1e100)
    val limitLeft = (for (start <- studentStart; limit <- aci.timeLimit) yield 
      (new Date(start.timeStarted).getTime() + limit * multiplier * 60000) - serverTime.getTime()).getOrElse(1e100)
    endLeft min limitLeft
  }

  def millisToHMS(millis: Double): String = {
    if (millis >= 1e100) "Unlimited" else {
      val totSecs = millis.toInt/1000
      val seconds = totSecs % 60
      val minutes = (totSecs / 60) % 60
      val hours = totSecs / 3600
      s"$hours:$minutes:$seconds"
    }
  }

  def assessmentViewable(aci: AssessmentCourseInfo, serverTime: Date): Boolean = {
    aci.start.map(new Date(_)).map(_.getTime() < serverTime.getTime()).getOrElse(true)
  }
}