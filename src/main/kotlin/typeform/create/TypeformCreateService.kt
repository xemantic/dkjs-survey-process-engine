/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.inject.Named

@Service
interface TypeformCreateService {

  fun createForm(payload: String): Flow<String>

}

@Service
class KtorTypeformCreateService @Inject constructor(
  @Named("typeformHttpClient") private val client: HttpClient
) : TypeformCreateService {

  override fun createForm(payload: String): Flow<String> {
    return flow {
      // TODO it's not a proper URL
      client.get<String>("http://typeform.api/create")
    }
  }

}
