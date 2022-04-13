/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import org.slf4j.Logger
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import javax.inject.Inject

class AlertEmailSender @Inject constructor(
  private val logger: Logger,
  private val config: MailConfig,
  private val sender: JavaMailSender
) {

  fun sendAlertEmail(subject: String, body: String) {
    val message = SimpleMailMessage().apply {
      setFrom(config.from)
      setTo(config.sendAlertsTo)
      setSubject(subject)
      setText(body)
    }
    try {
      sender.send(message)
    } catch (e: MailException) {
      logger.error("Could not send alert email", e)
    }
  }

}
