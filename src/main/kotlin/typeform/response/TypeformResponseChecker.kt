/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.response

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component
class TypeformResponseChecker @Inject constructor(
  private val service: TypeformResponseService
) {

  fun countSurveys(
    formId: String,
    projectId: String
  ): Int = runBlocking {
    service.countResponses(formId, projectId)
  }

}
