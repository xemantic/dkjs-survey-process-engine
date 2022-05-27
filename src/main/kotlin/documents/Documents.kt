/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.documents

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.SurveyType
import de.dkjs.survey.model.goalsToSequenceOfSmallLetters
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotEmpty

@Validated
@ConfigurationProperties("documents")
@ConstructorBinding
data class DocumentsConfig(

  @get:NotEmpty
  val urlBase: String

)

@Singleton
@Component
class SurveyPdfDocumentsLinkGenerator(
  @Inject private val config: DocumentsConfig
) {

  fun generate(project: Project, surveyType: SurveyType) = if (project.isGoalG) {
    "${config.urlBase}quali${surveyType.pdfLinkFix}"
  } else {
    "${config.urlBase}${surveyType.pdfLinkFix}-${goalsToSequenceOfSmallLetters(project.goals)}"
  }

}

private val SurveyType.pdfLinkFix get() = when (this) {
  SurveyType.IMPULS -> "imp"
  SurveyType.PRE -> "pre"
  SurveyType.POST -> "repo"
}
