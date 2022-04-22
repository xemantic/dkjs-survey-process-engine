/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
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
