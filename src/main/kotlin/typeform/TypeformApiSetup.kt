/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform

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

  @NotEmpty
  val surveyURL: String,

  val forms: Forms

) {

  data class Forms(

    @NotEmpty
    val pre: String,

    @NotEmpty
    val post: String,

    @NotEmpty
    val gPre: String,

    @NotEmpty
    val gPost: String

  )

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

  @PreDestroy
  fun destroy() {
    client.close()
  }

}
