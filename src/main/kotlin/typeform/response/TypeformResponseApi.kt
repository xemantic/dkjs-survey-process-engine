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

package de.dkjs.survey.typeform.response

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.springframework.stereotype.Component
import javax.inject.Named
import javax.inject.Singleton

@Serializable
data class ResponsePage(

  @SerialName("total_items")
  val totalItems: Int,

  @SerialName("page_count")
  val pageCount: Int,

  @SerialName("items")
  val items: List<Response>?

)

@Serializable
data class Response(

  @SerialName("response_id")
  val responseId: String,

)

interface TypeformResponseService {

  suspend fun countResponses(
    formId: String,
    projectId: String
  ): Int

}

@Singleton
@Component
class KtorTypeformResponseService constructor(
  @Named("typeformHttpClient") private val client: HttpClient,
  private val logger: Logger
) : TypeformResponseService {

  override suspend fun countResponses(
    formId: String,
    projectId: String
  ): Int {
    logger.info("Counting typeform responses, project: $projectId, form: $formId")
    val count = client.get("https://api.typeform.com/forms/$formId/responses") {
      parameter("fields", "hidden")
      parameter("query", projectId)
    }.body<ResponsePage>().totalItems
    logger.info("Counted typeform responses, project: $projectId, form: $formId, count: $count")
    return count
  }

}
