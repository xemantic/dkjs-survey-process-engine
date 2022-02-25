/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import javax.inject.Inject

//@DkjsSurveyProcessEngineTest
//class SurveyEmailSenderTest @Inject constructor(
//  private var surveyEmailSender: SurveyEmailSender,
//  private var mailSender: MailSender,
//) {

/**
 * Tests sending a non-template based simple HTML e-mail
 */
@DkjsSurveyProcessEngineTest
class SurveyEmailSenderTest {

  @Inject
  private lateinit var config: MailConfig

  @Inject
  private lateinit var mailSender: JavaMailSender

  @Test
  fun `should send email through configured provider`() {
    // given

    val message = mailSender.createMimeMessage()

    val mimeMessage = MimeMessageHelper(
      message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8"
    ).apply {
      setFrom(config.from)
      setTo(config.from)
      setSubject("test subject")
      setText("""<html><body>test content</body></html>""", true)
    }

    // when

    // then no error should be thrown
    verify {
      mailSender.send(message)
    }
    //confirmVerified(mailSender)
  }

//  @Test
//  @DisplayName("Send test")
//  fun testSend() {
//    GreenMailUtil.sendTextEmailTest("to@localhost", "from@localhost", "some subject", "some body")
//    val receivedMessages: Array<MimeMessage> = greenMail.receivedMessages
//    val receivedMessage = receivedMessages[0]
//    assertEquals("some body", GreenMailUtil.getBody(receivedMessage))
//  }

}
