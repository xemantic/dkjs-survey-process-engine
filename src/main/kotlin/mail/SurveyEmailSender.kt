/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.ScenarioType
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

interface SurveyEmailSender {

  fun send(
    template: MailType,
    project: Project,
    scenarioType: ScenarioType
  )

}

@Singleton
@Component
class DefaultSurveyEmailSender @Inject constructor(
  private val config: MailConfig,
  private val mailGenerator: MailGenerator,
  private val mailSender: JavaMailSender
) : SurveyEmailSender {

  override fun send(
    template: MailType,
    project: Project,
    scenarioType: ScenarioType
  ) {
    val mail = mailGenerator.generate(template, project, scenarioType)

    val message = mailSender.createMimeMessage()
    val mimeMessage = MimeMessageHelper(
      message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8"
    ).also {
      it.setFrom(config.from)
      it.setTo(project.contactPerson.email)
      it.setSubject(mail.subject)
      it.setText(mail.bodyHTML, true)

      // Inline image
      // it.setText("my text <img src='cid:myLogo'>", true)
      // it.addInline("myLogo", ClassPathResource("img/mylogo.gif"))

      // Attachment
      // it.addAttachment("myDocument.pdf", ClassPathResource("doc/myDocument.pdf"))
    }

    mailSender.send(message)
  }
}
