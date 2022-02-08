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
    message.setFrom(config.from)
    message.setTo(project.contactPerson.email)
    message.setSubject(mail.subject)
    message.setText(mail.bodyText)
    // TODO: Replace `SimpleMailMessage` with `MimeMessage` as shown in
    // https://springhow.com/spring-boot-email-thymeleaf/
    // TODO: use `mail.bodyHTML`
    mailSender.send(message)
  }

}
