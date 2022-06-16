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

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.SurveyType
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton
import javax.mail.internet.MimeMessage

interface SurveyEmailSender {

  /**
   * Sends email based on the the specified input.
   *
   * @param project the project related to this email.
   * @param mailType the email template.
   * @param surveyType the survey type.
   * @throws org.springframework.mail.MailException
   */
  fun send(
    project: Project,
    mailType: MailType,
    surveyType: SurveyType,
  )

}

/**
 * The [SurveyEmailSender] implementation using spring [JavaMailSender]
 *
 * If it needs to be extended in the future with more advanced formatting, here are some hints:
 *
 * ```
 *   // Inline image
 *   setText("my text <img src='cid:myLogo'>", true)
 *   addInline("myLogo", ClassPathResource("img/mylogo.gif"))
 *
 *   // Attachment
 *   addAttachment("myDocument.pdf", ClassPathResource("doc/myDocument.pdf"))
 * ```
 */
@Singleton
@Component
class DefaultSurveyEmailSender @Inject constructor(
  private val config: MailConfig,
  private val mailGenerator: MailGenerator,
  private val mailSender: JavaMailSender
) : SurveyEmailSender {

  override fun send(
    project: Project,
    mailType: MailType,
    surveyType: SurveyType
  ) {
    val mailData = mailGenerator.generate(project, mailType, surveyType)
    val message = newMessage(mailData, project.contactPerson.email)
    mailSender.send(message)
  }

  private fun newMessage(mailData: MailData, to: String): MimeMessage =
    mailSender.createMimeMessage().also {
      MimeMessageHelper(
        it,
        MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
        "UTF-8"
      ).run {
        setFrom(config.from)
        setTo(to)
        setSubject(mailData.subject)
        setText(mailData.bodyHTML, true)
      }
    }

}
