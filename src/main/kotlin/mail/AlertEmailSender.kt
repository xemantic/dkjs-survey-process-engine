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

import de.dkjs.survey.engine.DkjsConfig
import org.slf4j.Logger
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component
class AlertEmailSender @Inject constructor(
  private val logger: Logger,
  private val dkjsConfig: DkjsConfig,
  private val config: MailConfig,
  private val sender: JavaMailSender
) {

  fun sendAlertEmail(subject: String, body: String) {
    val message = SimpleMailMessage().apply {
      setFrom(config.from)
      setTo(config.sendAlertsTo)
      setSubject("[${dkjsConfig.environment} Surveys] $subject")
      setText(body)
    }
    try {
      sender.send(message)
    } catch (e: MailException) {
      logger.error("Could not send alert email", e)
    }
  }

}
