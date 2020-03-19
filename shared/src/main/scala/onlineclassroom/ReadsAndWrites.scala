package onlineclassroom

import play.api.libs.json._

object ReadsAndWrites {
  implicit val loginDataWrites = Json.writes[LoginData]
  implicit val loginDataReads = Json.reads[LoginData]

  implicit val clientToServerMessageWrites = Json.writes[ClientToServerMessage]
  implicit val clientToServerMessageReads = Json.reads[ClientToServerMessage]

  implicit val userDataWrites = Json.writes[UserData]
  implicit val userDataReads = Json.reads[UserData]

  implicit val courseInfoWrites = Json.writes[CourseInfo]
  implicit val courseInfoReads = Json.reads[CourseInfo]
}