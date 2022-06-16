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

import de.dkjs.survey.documents.SurveyPdfDocumentsLinkGenerator
import de.dkjs.survey.model.*
import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import de.dkjs.survey.time.dkjsDate
import de.dkjs.survey.time.parseDkjsDate
import de.dkjs.survey.typeform.link.TypeformSurveyLinkGenerator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.util.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * [MailGenerator] integration test.
 *
 * Verifies that an e-mail subject and html content is
 * correctly generated based on a [Project] and a [MailType].
 */
@DkjsSurveyProcessEngineTest
class MailGeneratorTest {

  @Inject
  private lateinit var mailGenerator: MailGenerator

  @Inject
  private lateinit var typeformSurveyLinkGenerator: TypeformSurveyLinkGenerator

  @Inject
  private lateinit var surveyPdfDocumentsLinkGenerator: SurveyPdfDocumentsLinkGenerator

  @Test
  fun `should generate mail from project data and template`() {
    // given
    val project = Project(
      name = "Foo",
      id = "42",
      contactPerson = ContactPerson(
        pronoun = "Herr",
        firstName = "Max",
        lastName = "Mustermann",
        email = "max@musterman.de"
      ),
      start = parseDkjsDate("15.01.2022"),
      end = parseDkjsDate("30.01.2022"),
      //goals = setOf(Goal.A),
      // next values will not influence mail
      //goals = setOf(Goal(1)),
      goals = setOf(1),
      //participantCount = 42,
      surveyProcess = SurveyProcess(
        id = "42",
        start = LocalDateTime.now(),
        phase = SurveyProcess.Phase.ACTIVE
      ),
      status = "foo",
      provider = Provider(
        "123",
        "foo"
      ),
      participants = Participants(
        0, 0, 0, 0, 0,
        0
      )
    )

    // when
    val mail = mailGenerator.generate(
      project, MailType.INFOMAIL_PRE_POST, SurveyType.PRE
    )

    // TODO which SurveyType should be chosen?
    val surveyType = SurveyType.PRE
    val typeformLink = typeformSurveyLinkGenerator.generate(project, surveyType)
    val pdfLink = surveyPdfDocumentsLinkGenerator.generate(project, surveyType)

    // then
    mail.subject shouldBe "Informationen zur Evaluation Ihres " +
            "AUF!leben-Projekts ${project.name}, " +
            "Projektnr.: ${project.id}"
    mail.bodyHTML shouldContain "evaluation.aufleben@dkjs.de"
    mail.bodyHTML shouldContain project.name
    mail.bodyHTML shouldContain project.id
    mail.bodyHTML shouldContain project.start.dkjsDate
    mail.bodyHTML shouldContain project.end.dkjsDate
    mail.bodyHTML shouldContain typeformLink.escapeHTML()
    mail.bodyHTML shouldContain pdfLink.escapeHTML()
  }

}
