/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

@DkjsSurveyProcessEngineTest
class AreWeASpammerTest(
  @Autowired private val mailSender: JavaMailSender
) {

  @Test
  @Disabled // intentionally disabled as it is a one time action run to verify
  fun `send email to mail tester`() {
    val from = "evaluation@dkjs.de"
    val to = "test-bv1c0g09p@srv1.mail-tester.com" // replace with the mail supplied by the tester
    val message = mailSender.createMimeMessage()
    MimeMessageHelper(
      message,
      MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
      "UTF-8"
    ).run {
      setFrom(from)
      setTo(to)
      setSubject("test")
      setText("test")
    }
    mailSender.send(message)
  }

}
