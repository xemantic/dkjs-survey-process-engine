/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.Project
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

interface SurveyEmailSender {

  fun send(template: MailType, project: Project)

}

@Singleton
@Component
class DefaultSurveyEmailSender @Inject constructor(
  private val config: MailConfig,
  private val mailGenerator: MailGenerator,
  private val mailSender: MailSender,
) : SurveyEmailSender {

  override fun send(template: MailType, project: Project) {
    val mail = mailGenerator.generate(template, project)
    val message = SimpleMailMessage()
    message.from = config.from
    message.setTo(project.email)
    message.subject = mail.subject
    message.text = mail.body
    mailSender.send(message)
  }

}
