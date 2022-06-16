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

package de.dkjs.survey.web

import de.dkjs.survey.mail.MailType
import de.dkjs.survey.model.*
import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import de.dkjs.survey.test.SurveyProcessTestBase
import de.dkjs.survey.time.dkjsDateTime
import io.mockk.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@DkjsSurveyProcessEngineTest
class ProjectCsvWebUploadTest : SurveyProcessTestBase() {

  @Test
  @Disabled // test disabled as it is flaky on github actions
  fun `should upload project CSV file and start the process`() {
    // given
    val projectId = "test case 1"
    val now = now()
    val start = now + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 13.days
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsvThroughHttp("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)     // conditional on typeform check SurveyType.POST
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

}
