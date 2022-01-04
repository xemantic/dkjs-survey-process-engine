/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import java.io.File
import java.util.*
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull

enum class MailType {

  INFOMAIL_RETRO,
  REMINDER_1_RETRO,
  REMINDER_2_RETRO,
  INFOMAIL_PRE_POST,
  REMINDER_1_T0,
  REMINDER_2_T0,
  INFOMAIL_T1,
  REMINDER_1_T1,
  REMINDER_1_T1_RETRO,
  REMINDER_2_T1_RETRO

}

data class MailTemplateData(
  val subject: String,
  val body :String
)

data class MailData(
  val subject: String,
  val body :String
)

@Component
@ConfigurationProperties(prefix = "mail")
@Validated
class MailConfig {

  @NotNull
  @Email
  lateinit var from: String

  @NotNull
  lateinit var templateDir: String

}

@Configuration
open class MailTemplateConfig {

  @Singleton
  @Bean
  @Named("templates")
  open fun templates(
    @Value("\${mail.templateDir}") templateDir: String
  ) = EnumMap(MailType.values().associate {
    val folder = it.name.lowercase()
    val dir = File(templateDir, folder)
    it to MailTemplateData(
      File(dir, "subject.txt").readText(),
      File(dir, "body.txt").readText()
    )
  })

}
