/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import javax.inject.Inject

//@DkjsSurveyProcessEngineTest
//class SurveyEmailSenderTest @Inject constructor(
//  private var surveyEmailSender: SurveyEmailSender,
//  private var mailSender: MailSender,
//) {

@DkjsSurveyProcessEngineTest
class SurveyEmailSenderTest {

  @Inject
  private lateinit var surveyEmailSender: SurveyEmailSender

  @Inject
  private lateinit var mailSender: MailSender

  @Test
  fun `should send email through configured provider`() {
    // given

//    val project = Project(
//      projectName = "Foo",
//      projectNumber = "42",
//      projectContact = "Herr Max Mustermann",
//      startDate = parseDate("20220115"),
//      endDate = parseDate("20220130"),
//      //goals = setOf(Goal.A),
//      email = "max@musterman.de",
//      // next values will not influence mail
//      //goals = setOf(Goal(1)),
//      goals = setOf(1),
//      participantCount = 42,
//      surveyProcess = SurveyProcess(
//        phase = SurveyProcess.Phase.PERSISTED,
//        notifications = mutableListOf()
//      )
//    )

    // when
    //surveyEmailSender.send(MailType.INFOMAIL_PRE_POST, project)

    // then no error should be thrown
    verify {
      mailSender.send(SimpleMailMessage())

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
