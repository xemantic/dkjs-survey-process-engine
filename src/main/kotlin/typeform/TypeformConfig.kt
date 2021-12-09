/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Component
@ConfigurationProperties(prefix = "typeform")
@Validated
class TypeformConfig {

  @NotNull
  @NotEmpty
  lateinit var clientId: String

}
