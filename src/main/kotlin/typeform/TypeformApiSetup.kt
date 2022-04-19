/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.Scenario
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
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
    val retro: String,

    @get:NotEmpty
    val prePost: String,

    @get:NotEmpty
    val goalGRetro: String,

    @get:NotEmpty
    val goalGPrePost: String

  ) {

    fun getFormId(project: Project, type: Scenario): String = when (type) {
      Scenario.RETRO -> if (project.isGoalG) goalGRetro else retro
      Scenario.PRE_POST -> if (project.isGoalG) goalGPrePost else prePost
    }

  }

}

@Configuration
class TypeformApiSetup(@Inject private val config: TypeformConfig) {

  private val client: HttpClient = HttpClient(CIO) {
    install(JsonFeature) {
      serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
      })
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
