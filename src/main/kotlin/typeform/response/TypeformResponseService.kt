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
interface TypeformResponseService {

  fun getResponses(payload: String): Flow<String>

}

@Service
class KtorTypeformResponseService @Inject constructor(
  @Named("typeformHttpClient") private val client: HttpClient
) : TypeformResponseService {

  override fun getResponses(payload: String): Flow<String> {
    return flow {
      // TODO it's not a proper URL
      client.get<String>("https://api.typeform.com/forms/aNmTHQY7/responses?page_size=1000")
    }
  }

}


//url = "https://api.typeform.com/forms/aNmTHQY7/responses?page_size=1000"
//key = "tfp_2AcSZK8rWRuMhRj5c7MLGrT27xgzNhKsBi9dYwYRKvQE_hkdJahGZS8sa"