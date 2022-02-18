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

/**
 * Different types of e-mails that can be sent on different
 * stages of a project.
 */
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
 * e-mail content coming from processed e-mail templates
 */
data class MailData(
  val subject: String,
  val bodyHTML: String
)

@ConstructorBinding
@ConfigurationProperties("mail")
@Validated
data class MailConfig(

  @NotEmpty
  @Email
  val from: String,

)
