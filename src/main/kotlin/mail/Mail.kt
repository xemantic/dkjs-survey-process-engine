/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import java.io.File
import java.util.*
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

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

/**
 * Raw e-mail templates as read from disk
 */
data class MailTemplateData(
  val subject: String,
  val bodyText: String,
  val bodyHTML: String
)

/**
 * Processed e-mail templates after replacing tokens
 */
data class MailData(
  val subject: String,
  val bodyText: String,
  val bodyHTML: String
)

@ConstructorBinding
@ConfigurationProperties("mail")
@Validated
data class MailConfig(

  @NotEmpty
  @Email
  val from: String,

  @NotEmpty
  val templateDir: String

)

@Configuration
class MailTemplateSetup {

  @Singleton
  @Bean
  @Named("templates")
  fun templates(
    config: MailConfig
  ) = EnumMap(MailType.values().associate {
    val folder = it.name.lowercase()
    val dir = File(config.templateDir, folder)
    it to MailTemplateData(
      File(dir, "subject.txt").readText(),
      File(dir, "body.txt").readText(),
      File(dir, "body.html").readText()
    )
  })

}
