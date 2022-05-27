/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.SurveyType
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@ConstructorBinding
@ConfigurationProperties("typeform")
@Validated
data class TypeformConfig(

  @get:NotEmpty
  val clientId: String,

  @get:NotEmpty
  val urlBase: String,

  @get:Valid
  val forms: Forms

) {

  data class Forms(

    @get:NotEmpty
    val pre: String,

    @get:NotEmpty
    val post: String,

    @get:NotEmpty
    val goalGPre: String,

    @get:NotEmpty
    val goalGPost: String,

    @get:NotEmpty
    val impuls: String

  ) {

    fun getFormId(project: Project, type: SurveyType): String = when (type) {
      SurveyType.IMPULS -> impuls
      SurveyType.PRE -> if (project.isGoalG) goalGPre else pre
      SurveyType.POST -> if (project.isGoalG) goalGPost else post
    }

  }

}

@Configuration
class TypeformApiSetup(@Inject private val config: TypeformConfig) {

  private val client: HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
      json(Json {
        ignoreUnknownKeys = true
      })
    }
    install(HttpTimeout) {
      requestTimeoutMillis = 30000
      connectTimeoutMillis = 30000
      socketTimeoutMillis = 30000
    }
    install(HttpRequestRetry) {
      retryOnServerErrors(maxRetries = 5)
      exponentialDelay()
    }
    install(Auth) {
      bearer {
        loadTokens {
          BearerTokens(
            accessToken = config.clientId,
            refreshToken = ""
          )
        }
      }
    }
  }

  @Singleton
  @Bean
  @Named("typeformHttpClient")
  fun typeformHttpClient(): HttpClient = client

  @Suppress("unused")
  @PreDestroy
  fun destroy() {
    client.close()
  }

}
