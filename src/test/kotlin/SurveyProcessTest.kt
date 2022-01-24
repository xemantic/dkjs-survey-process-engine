/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.mail.MailType
import de.dkjs.survey.mail.SurveyEmailSender
import de.dkjs.survey.model.ProjectRepository
import de.dkjs.survey.model.SurveyProcess
import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import de.dkjs.survey.test.uploadProjectsCsv
import de.dkjs.survey.test.sleepForMaximalProcessDuration
import de.dkjs.survey.time.dkjsDate
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import javax.inject.Inject

@DkjsSurveyProcessEngineTest
class SurveyProcessTest {

  // test cases: start

  @Test
  fun `current date greater than project start date`() {
    // given
    val now = LocalDateTime.now()
    val start = now.minusDays(1)
    val end = start.plusDays(30)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDate}";"${end.dkjsDate}"
    """)

    // then
      .expectStatus().isOk

    sleepForMaximalProcessDuration()

    val process = repository.findByIdOrNull("4021000014 -1")
    process shouldNotBe null
    process!!.surveyProcess shouldNotBe null
    process.surveyProcess!!.phase shouldBe SurveyProcess.Phase.FINISHED
    verifyOrder {
      surveyEmailSender.send(MailType.INFOMAIL_PRE_POST, any())
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_1_T0, any())
    }
  }

  // test cases: end

  @Inject private lateinit var logger: Logger
  @Inject private lateinit var surveyEmailSender: SurveyEmailSender
  @Inject private lateinit var repository: ProjectRepository
  @Inject private lateinit var client: WebTestClient

  fun uploadingProjectsCsv(csv: String) = client.uploadProjectsCsv(csv)

  fun sleepForMaximalProcessDuration() = sleepForMaximalProcessDuration(logger, 3)

}
