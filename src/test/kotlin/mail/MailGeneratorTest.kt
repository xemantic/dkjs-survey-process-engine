/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.documents.SurveyDocumentPdfLinkGenerator
import de.dkjs.survey.model.*
import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import de.dkjs.survey.time.dkjsDate
import de.dkjs.survey.time.parseDkjsDate
import de.dkjs.survey.typeform.link.TypeformSurveyLinkGenerator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.util.*
import org.junit.jupiter.api.Test
import javax.inject.Inject

// TODO review this test

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
  private lateinit var surveyDocumentPdfLinkGenerator: SurveyDocumentPdfLinkGenerator

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
      MailType.INFOMAIL_PRE_POST, project, Scenario.PRE_POST
    )

    // TODO which SenarioType should be chosen?
    val typeformLink = typeformSurveyLinkGenerator.generate(
      project, Scenario.PRE_POST
    )
    val pdfLink = surveyDocumentPdfLinkGenerator.generate(project)

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
