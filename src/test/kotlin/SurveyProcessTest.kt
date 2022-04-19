/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.mail.MailType
import de.dkjs.survey.mail.SurveyEmailSender
import de.dkjs.survey.model.ProjectRepository
import de.dkjs.survey.model.Scenario
import de.dkjs.survey.model.SurveyProcess
import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import de.dkjs.survey.test.uploadProjectsCsv
import de.dkjs.survey.test.sleepForMaximalProcessDuration
import de.dkjs.survey.time.dkjsDateTime
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.verifyOrder
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime

// TODO update this test with more test cases

@DkjsSurveyProcessEngineTest
class SurveyProcessTest @Autowired constructor(
  val logger: Logger,
  val surveyEmailSender: SurveyEmailSender,
  val projectRepository: ProjectRepository,
  val client: WebTestClient
) {

  // test cases: start

  @Test
  @Disabled
  fun `test case 1 - project data gets into the system before the project starts`() {
    // given
    val now = LocalDateTime.now()
    val start = now.plusSeconds(1)
    val end = start.plusSeconds(1)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)

    // then
      .expectStatus().isOk

    sleepForMaximalProcessDuration()

    val project = projectRepository.findByIdOrNull("4021000014 -1")
    project shouldNotBe null
    project!!.surveyProcess shouldNotBe null
    project.surveyProcess!!.phase shouldBe SurveyProcess.Phase.FINISHED
    verifyOrder {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO,  Scenario.PRE_POST, any())
    }
  }

  // test cases: end

  fun uploadingProjectsCsv(csv: String) = client.uploadProjectsCsv(csv)

  fun sleepForMaximalProcessDuration() = sleepForMaximalProcessDuration(logger, 3)

}
