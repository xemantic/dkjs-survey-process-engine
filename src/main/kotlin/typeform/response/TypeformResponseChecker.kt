/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.response

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component
class TypeformResponseChecker @Inject constructor(
  @Named("typeformHttpClient") private val client: HttpClient,
  @Value("\${typeform.formId}") private val formId: String
) {

  // TODO implement this service
  fun countSurveys(projectName: String): Int = 0
//  runBlocking {
////    (client.request("https://api.typeform.com/forms/$formId/responses") {
////        header("Bearer", "tfp_2AcSZK8rWRuMhRj5c7MLGrT27xgzNhKsBi9dYwYRKvQE_hkdJahGZS8sa")
////      }).
////    0
//    return 0
//  }

}
