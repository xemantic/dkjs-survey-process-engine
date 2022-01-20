/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.*
import de.dkjs.survey.time.parseDkjsDate
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
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
      start = parseDkjsDate("2022.01.15"),
      end = parseDkjsDate("2022.01.30"),
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
    val templates = EnumMap<MailType, MailTemplateData>(MailType::class.java)
    templates[MailType.INFOMAIL_PRE_POST] = MailTemplateData(
      subject = "Information regarding your AUF!leben-project {projectName}",
      body = """
        Dear {projectContact},

        We are contacting you ...

        Regarding project {projectName} (project number: {projectNumber}, {startDate} - {endDate}).

        Please fill the form:

        online: {typeformLink}

        as PDF: {pdfLink}

        Best regards,
        DKJS
      """.trimIndent()
    )
    val mailGenerator = MailGenerator(
      templates,
      TypeformSurveyLinkGenerator("https://typeform/form"),
      PdfSurveyLinkGenerator("https://dkjs.de/pdfs")
    )

    // when
    val mail = mailGenerator.generate(MailType.INFOMAIL_PRE_POST, project)

    // then
    mail.subject shouldBe "Information regarding your AUF!leben-project Foo"
    mail.body shouldBe """
        Dear Herr Max Mustermann,

        We are contacting you ...

        Regarding project Foo (project number: 42, 15.01.2022 - 30.01.2022).

        Please fill the form:

        online: https://typeform/form?projectNumber=42&blocks=Foo

        as PDF: https://dkjs.de/pdfs

        Best regards,
        DKJS
    """.trimIndent()

  }

}
