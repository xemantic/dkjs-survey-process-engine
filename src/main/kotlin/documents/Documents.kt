/*
 * dkjs-survey-process-engine - https://www.dkjs.de/
 * Copyright (C) 2022 Kazimierz Pogoda / https://xemantic.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
