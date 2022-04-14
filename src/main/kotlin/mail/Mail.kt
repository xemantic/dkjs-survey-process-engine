/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

/**
 * Different types of e-mails that can be sent on different
 * stages of a project.
 */
enum class MailType {

  INFOMAIL_PRE_POST,
  REMINDER_1_T0,
  REMINDER_2_T0,
  INFOMAIL_T1,
  REMINDER_1_T1,
  REMINDER_1_T1_RETRO,
  REMINDER_2_T1_RETRO,
  INFOMAIL_RETRO,
  REMINDER_1_RETRO,
  REMINDER_2_RETRO,

}

/**
 * e-mail content coming from processed e-mail templates
 */
data class MailData(
  val subject: String,
  val bodyHTML: String
)

@Validated
@ConfigurationProperties("mail")
@ConstructorBinding
data class MailConfig(

  @get:NotEmpty
  @get:Email
  val from: String,

  @get:NotEmpty
  @get:Email
  val sendAlertsTo: String,

)
