/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.oauth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenInfo(

  @SerialName("access_token")
  val accessToken: String,

  @SerialName("expires_in")
  val expiresIn: Int,

  @SerialName("refresh_token")
  val refreshToken: String? = null,

  val scope: String,

  @SerialName("token_type")
  val tokenType: String,

  @SerialName("id_token")
  val idToken: String

)

// TODO it looks google specific, maybe can be removed
@Serializable
data class UserInfo(

  val id: String,

  val name: String,

  @SerialName("given_name")
  val givenName: String,

  @SerialName("family_name")
  val familyName: String,

  val picture: String,

  val locale: String

)
