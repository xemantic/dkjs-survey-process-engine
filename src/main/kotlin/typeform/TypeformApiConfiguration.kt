/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform

import de.dkjs.survey.oauth.TokenInfo
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Configuration
open class TypeformApiConfiguration @Inject constructor(
  @Value("\${typeform.clientId}") private val clientId: String
) {

  private lateinit var client: HttpClient

  @Singleton
  @Bean
  @Named("typeformHttpClient")
  open fun typeformHttpClient(): HttpClient {
    val authorizationCode = "1234"

    val tokenClient = HttpClient(CIO) {
      install(JsonFeature) {
        serializer = KotlinxSerializer()
      }
    }

    return HttpClient(CIO) {
      expectSuccess = false
      install(JsonFeature) {
        serializer = KotlinxSerializer()
      }
      install(Auth) {
        lateinit var tokenInfo: TokenInfo
        var refreshTokenInfo: TokenInfo

        bearer {
          loadTokens {
            tokenInfo = tokenClient.submitForm(
              url = "https://api.typeform.com/oauth/authorize",
              formParameters = Parameters.build {
                append("grant_type", "authorization_code")
                append("code", authorizationCode)
                append("client_id", clientId)
                //append("redirect_uri", redirectUri)
              }
            )
            BearerTokens(
              accessToken = tokenInfo.accessToken,
              refreshToken = tokenInfo.refreshToken!!
            )
          }

          refreshTokens { unauthorizedResponse: HttpResponse ->
            refreshTokenInfo = tokenClient.submitForm(
              url = "https://api.typeform.com/oauth/authorize",
              formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("client_id", clientId)
                append("refresh_token", tokenInfo.refreshToken!!)
              }
            )
            BearerTokens(
              accessToken = refreshTokenInfo.accessToken,
              refreshToken = tokenInfo.refreshToken!!
            )
          }
        }
      }
    }
  }

  @PreDestroy
  fun destroy() {
    client.close()
  }

}
