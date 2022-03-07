/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform

import de.dkjs.survey.model.ScenarioType
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
import javax.validation.constraints.NotEmpty

@ConstructorBinding
@ConfigurationProperties("typeform")
@Validated
data class TypeformConfig(

  @NotEmpty
  val clientId: String,

  @NotEmpty
  val linkBase: String,

  val forms: Forms

) {

  data class Forms(

    @NotEmpty
    val pre: String,

    @NotEmpty
    val post: String,

    @NotEmpty
    val goalGPre: String,

    @NotEmpty
    val goalGPost: String

  ) {

    fun getFormId(type: ScenarioType): String = when (type) {
      ScenarioType.PRE -> pre
      ScenarioType.POST -> post
      ScenarioType.GOAL_G_PRE -> goalGPre
      ScenarioType.GOAL_G_POST -> goalGPost
    }

  }

}

@Configuration
class TypeformApiSetup(@Inject private val config: TypeformConfig) {

  private val client: HttpClient = HttpClient(CIO) {
    install(JsonFeature) {
      serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
        isLenient = true
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
