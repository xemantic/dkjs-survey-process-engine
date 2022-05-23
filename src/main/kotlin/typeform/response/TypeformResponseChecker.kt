/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.response

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.SurveyType
import de.dkjs.survey.typeform.TypeformConfig
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component
class TypeformResponseChecker @Inject constructor(
  private val service: TypeformResponseService,
  private val config: TypeformConfig
) {

  fun countSurveys(project: Project, surveyType: SurveyType): Int = runBlocking {
    service.countResponses(
      config.forms.getFormId(project, surveyType),
      project.id
    )
  }

}
