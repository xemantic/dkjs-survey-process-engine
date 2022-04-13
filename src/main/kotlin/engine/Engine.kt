/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.engine

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@ConstructorBinding
@ConfigurationProperties("dkjs")
@Validated
data class DkjsConfig(

  // DEV, TEST, PROD, etc.
  @NotEmpty
  val environment: String

)
