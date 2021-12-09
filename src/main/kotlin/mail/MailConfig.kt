/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull

@Component
@ConfigurationProperties(prefix = "mail")
@Validated
class MailConfig {

  @NotNull
  @Email
  lateinit var from: String

  @NotNull
  lateinit var subject: String

  @NotNull
  lateinit var template: String

}
