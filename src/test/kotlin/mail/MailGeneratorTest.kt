/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.documents.DocumentsConfig
import de.dkjs.survey.model.*
import de.dkjs.survey.time.dkjsDate
import de.dkjs.survey.time.parseDkjsDate
import de.dkjs.survey.typeform.TypeformConfig
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.thymeleaf.spring5.SpringTemplateEngine
import java.util.*

/**
 * [MailGenerator] unit test.
 */
class MailGeneratorTest {

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
        phase = SurveyProcess.Phase.PERSISTED,
        notifications = mutableListOf()
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
    val mailGenerator = MailGenerator(
      TypeformSurveyLinkGenerator(
        TypeformConfig(
          clientId = "42",
          linkBase = "https://typeform/form"
        ),
      ),
      SurveyDocumentPdfLinkGenerator(
        DocumentsConfig(
          linkBase = "https://dkjs.de/pdfs"
        )
      ),
      SpringTemplateEngine()
    )

    // when
    val mail = mailGenerator.generate(MailType.INFOMAIL_PRE_POST, project)

    // then
    mail.subject shouldBe "Informationen zur Evaluation Ihres AUF!leben-Projekts ${project.name}, Projektnr.: ${project.id}"
    mail.bodyHTML shouldContain "evaluation.aufleben@dkjs.de"
    mail.bodyHTML shouldContain project.name
    mail.bodyHTML shouldContain project.id
    mail.bodyHTML shouldContain project.start.dkjsDate
    mail.bodyHTML shouldContain project.end.dkjsDate
  }

}
