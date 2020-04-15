package onlineclassroom

import play.api.libs.json._

object ReadsAndWrites {
  // implicit def tuple2Reads[A, B](implicit aReads: Reads[A], bReads: Reads[B]): Reads[Tuple2[A, B]] = Reads[Tuple2[A, B]] {
  //   case JsArray(arr) if arr.size == 2 => for {
  //     a <- aReads.reads(arr(0))
  //     b <- bReads.reads(arr(1))
  //   } yield (a, b)
  //   case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("Expected array of three elements"))))
  // }

  // implicit def tuple2Writes[A, B](implicit aWrites: Writes[A], bWrites: Writes[B]): Writes[Tuple2[A, B]] = new Writes[Tuple2[A, B]] {
  //   def writes(tuple: Tuple2[A, B]) = JsArray(Seq(aWrites.writes(tuple._1), bWrites.writes(tuple._2)))
  // }

  implicit val loginDataWrites = Json.writes[LoginData]
  implicit val loginDataReads = Json.reads[LoginData]

  implicit val userDataWrites = Json.writes[UserData]
  implicit val userDataReads = Json.reads[UserData]

  implicit val courseDataWrites = Json.writes[CourseData]
  implicit val courseDataReads = Json.reads[CourseData]

  implicit val passwordChangeDataWrites = Json.writes[PasswordChangeData]
  implicit val passwordChangeDataReads = Json.reads[PasswordChangeData]

  implicit val newUserDataWrites = Json.writes[NewUserData]
  implicit val newUserDataReads = Json.reads[NewUserData]

  implicit val newCourseDataWrites = Json.writes[NewCourseData]
  implicit val newCourseDataReads = Json.reads[NewCourseData]

  implicit val fullStudentDataWrites = Json.writes[FullStudentData]
  implicit val fullStudentDataReads = Json.reads[FullStudentData]

  implicit val assessmentCourseInfoWrites = Json.writes[AssessmentCourseInfo]
  implicit val assessmentCourseInfoReads = Json.reads[AssessmentCourseInfo]

  implicit val gradeFormulaWrites = Json.writes[GradeFormulaInfo]
  implicit val gradeFormulaReads = Json.reads[GradeFormulaInfo]

  implicit val courseGradeInformationWrites = Json.writes[CourseGradeInformation]
  implicit val courseGradeInformationReads = Json.reads[CourseGradeInformation]

  implicit val fullInstructorCourseDataWrites = Json.writes[FullInstructorCourseData]
  implicit val fullInstructorCourseDataReads = Json.reads[FullInstructorCourseData]

  implicit val multipleChoiceInfoWrites = Json.writes[MultipleChoiceInfo]
  implicit val multipleChoiceInfoReads = Json.reads[MultipleChoiceInfo]

  implicit val isReads = Json.reads[IntSpec]
  implicit val isWrites = Json.writes[IntSpec]

  implicit val dsReads = Json.reads[DoubleSpec]
  implicit val dsWrites = Json.writes[DoubleSpec]

  implicit val ssReads = Json.reads[StringSpec]
  implicit val ssWrites = Json.writes[StringSpec]

  implicit val lisReads = Json.reads[ListIntSpec]
  implicit val lisWrites = Json.writes[ListIntSpec]

  implicit val aisReads = Json.reads[ArrayIntSpec]
  implicit val aisWrites = Json.writes[ArrayIntSpec]

  implicit val lssReads = Json.reads[ListStringSpec]
  implicit val lssWrites = Json.writes[ListStringSpec]

  implicit val aaisReads = Json.reads[ArrayArrayIntSpec]
  implicit val aaisWrites = Json.writes[ArrayArrayIntSpec]

  implicit val aadsReads = Json.reads[ArrayArrayDoubleSpec]
  implicit val aadsWrites = Json.writes[ArrayArrayDoubleSpec]

  implicit val vsReads = Json.reads[VariableSpec]
  implicit val vsWrites = Json.writes[VariableSpec]

  implicit val referenceBoxWrites = Json.writes[ReferenceBox]
  implicit val referenceBoxReads = Json.reads[ReferenceBox]

  implicit val valueBoxWrites = Json.writes[ValueBox]
  implicit val valueBoxReads = Json.reads[ValueBox]

  implicit val doubleBoxWrites = Json.writes[DoubleBox]
  implicit val doubleBoxReads = Json.reads[DoubleBox]

  implicit val tripleBoxWrites = Json.writes[TripleBox]
  implicit val tripleBoxReads = Json.reads[TripleBox]

  implicit val arrayOfBoxesWrites = Json.writes[ArrayOfBoxes]
  implicit val arrayOfBoxesReads = Json.reads[ArrayOfBoxes]

  implicit val graphNodeWrites = Json.writes[GraphNode]
  implicit val graphNodeReads = Json.reads[GraphNode]

  implicit val connectorWrites = Json.writes[Connector]
  implicit val connectorReads = Json.reads[Connector]

  implicit val curveWrites = Json.writes[Curve]
  implicit val curveReads = Json.reads[Curve]

  implicit val textWrites = Json.writes[Text]
  implicit val textReads = Json.reads[Text]

  implicit val drawAnswerElementWrites = Json.writes[DrawAnswerElement]
  implicit val drawAnswerElementReads = Json.reads[DrawAnswerElement]

  implicit val shortAnswerInfoWrites = Json.writes[ShortAnswerInfo]
  implicit val shortAnswerInfoReads = Json.reads[ShortAnswerInfo]

  implicit val writeFunctionInfoWrites = Json.writes[WriteFunctionInfo]
  implicit val writeFunctionInfoReads = Json.reads[WriteFunctionInfo]

  implicit val writeLambdaInfoWrites = Json.writes[WriteLambdaInfo]
  implicit val writeLambdaInfoReads = Json.reads[WriteLambdaInfo]

  implicit val writeExpressionInfoWrites = Json.writes[WriteExpressionInfo]
  implicit val writeExpressionInfoReads = Json.reads[WriteExpressionInfo]

  implicit val drawingInfoWrites = Json.writes[DrawingInfo]
  implicit val drawingInfoReads = Json.reads[DrawingInfo]
  
  implicit val manualEntryInfoWrites = Json.writes[ManualEntryInfo]
  implicit val manualEntryInfoReads = Json.reads[ManualEntryInfo]

  implicit val problemInfoErrorWrites = Json.writes[ProblemInfoError]
  implicit val problemInfoErrorReads = Json.reads[ProblemInfoError]
  
  implicit val problemInfoWrites = Json.writes[ProblemInfo]
  implicit val problemInfoReads = Json.reads[ProblemInfo]

  implicit val multipleChoiceGradeInfoWrites = Json.writes[MultipleChoiceGradeInfo]
  implicit val multipleChoiceGradeInfoReads = Json.reads[MultipleChoiceGradeInfo]

  implicit val shortAnswerGradeInfoWrites = Json.writes[ShortAnswerGradeInfo]
  implicit val shortAnswerGradeInfoReads = Json.reads[ShortAnswerGradeInfo]

  implicit val writeFunctionGradeInfoWrites = Json.writes[WriteFunctionGradeInfo]
  implicit val writeFunctionGradeInfoReads = Json.reads[WriteFunctionGradeInfo]

  implicit val writeLambdaGradeInfoWrites = Json.writes[WriteLambdaGradeInfo]
  implicit val writeLambdaGradeInfoReads = Json.reads[WriteLambdaGradeInfo]

  implicit val writeExpressionGradeInfoWrites = Json.writes[WriteExpressionGradeInfo]
  implicit val writeExpressionGradeInfoReads = Json.reads[WriteExpressionGradeInfo]

  implicit val drawingGradeInfoWrites = Json.writes[DrawingGradeInfo]
  implicit val drawingGradeInfoReads = Json.reads[DrawingGradeInfo]

  implicit val manualEntryGradeInfoWrites = Json.writes[ManualEntryGradeInfo]
  implicit val manualEntryGradeInfoReads = Json.reads[ManualEntryGradeInfo]

  implicit val problemGradeInfoWrites = Json.writes[ProblemGradeInfo]
  implicit val problemGradeInfoReads = Json.reads[ProblemGradeInfo]

  implicit val problemSpecWrites = Json.writes[ProblemSpec]
  implicit val problemSpecReads = Json.reads[ProblemSpec]

  implicit val assessmentDataWrites = Json.writes[AssessmentData]
  implicit val assessmentDataReads = Json.reads[AssessmentData]

  implicit val problemAssessmentAssocWrites = Json.writes[ProblemAssessmentAssociation]
  implicit val problemAssessmentAssocReads = Json.reads[ProblemAssessmentAssociation]

  implicit val studentAssessmentStartWrites = Json.writes[StudentAssessmentStart]
  implicit val studentAssessmentStartReads = Json.reads[StudentAssessmentStart]

  implicit val multipleChoiceAnswerWrites = Json.writes[MultipleChoiceAnswer]
  implicit val multipleChoiceAnswerReads = Json.reads[MultipleChoiceAnswer]

  implicit val shortAnswerAnswerWrites = Json.writes[ShortAnswerAnswer]
  implicit val shortAnswerAnswerReads = Json.reads[ShortAnswerAnswer]

  implicit val writeFunctionAnswerWrites = Json.writes[WriteFunctionAnswer]
  implicit val writeFunctionAnswerReads = Json.reads[WriteFunctionAnswer]

  implicit val writeLambdaAnswerWrites = Json.writes[WriteLambdaAnswer]
  implicit val writeLambdaAnswerReads = Json.reads[WriteLambdaAnswer]

  implicit val writeExpressionAnswerWrites = Json.writes[WriteExpressionAnswer]
  implicit val writeExpressionAnswerReads = Json.reads[WriteExpressionAnswer]

  implicit val drawingAnswerWrites = Json.writes[DrawingAnswer]
  implicit val drawingAnswerReads = Json.reads[DrawingAnswer]

  implicit val manualEntryAnswerWrites = Json.writes[ManualEntryAnswer]
  implicit val manualEntryAnswerReads = Json.reads[ManualEntryAnswer]

  implicit val problemAnswerErrorWrites = Json.writes[ProblemAnswerError]
  implicit val problemAnswerErrorReads = Json.reads[ProblemAnswerError]
  
  implicit val problemAnswerWrites = Json.writes[ProblemAnswer]
  implicit val problemAnswerReads = Json.reads[ProblemAnswer]

  implicit val studentProblemSpecWrites = Json.writes[StudentProblemSpec]
  implicit val studentProblemSpecReads = Json.reads[StudentProblemSpec]

  implicit val saveAnswerInfoWrites = Json.writes[SaveAnswerInfo]
  implicit val saveAnswerInfoReads = Json.reads[SaveAnswerInfo]

  implicit val gradeDataWrites = Json.writes[GradeData]
  implicit val gradeDataReads = Json.reads[GradeData]

  implicit val gradeAnswerWrites = Json.writes[GradeAnswer]
  implicit val gradeAnswerReads = Json.reads[GradeAnswer]

  implicit val gradingProblemDataWrites = Json.writes[GradingProblemData]
  implicit val gradingProblemDataReads = Json.reads[GradingProblemData]

  implicit val assessmentGradingDataWrites = Json.writes[AssessmentGradingData]
  implicit val assessmentGradingDataReads = Json.reads[AssessmentGradingData]
  
}