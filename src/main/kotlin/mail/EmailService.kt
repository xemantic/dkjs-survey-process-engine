/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class EmailService @Inject constructor(
  private val mailSender: JavaMailSender,
  private val config: MailConfig
) {

  fun sendEmail(to: String, link: String) {
    val text = config.template.replace("{link}", link)
    val message = SimpleMailMessage()
    message.from = config.from
    message.setTo(to)
    message.subject = config.subject
    message.text = text
    mailSender.send(message)
  }

}
