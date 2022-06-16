/*
 * dkjs-survey-process-engine - https://www.dkjs.de/
 * Copyright (C) 2022 Kazimierz Pogoda / https://xemantic.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
