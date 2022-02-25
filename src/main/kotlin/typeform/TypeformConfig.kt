/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
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
)
