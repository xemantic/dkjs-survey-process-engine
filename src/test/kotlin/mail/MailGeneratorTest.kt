/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

/**
 * [MailGenerator] unit test.
 */
class MailGeneratorTest {

  @Test
  fun `should generate mail from project data and template`() {
    // given
    val project = Project(
      projectName = "Foo",
      projectNumber = "42",
      projectContact = "Herr Max Mustermann",
      startDate = parseDate("20220115"),
      endDate = parseDate("20220130"),
      //goals = setOf(Goal.A),
      email = "max@musterman.de",
      // next values will not influence mail
      goals = setOf(Goal(1)),
      participantCount = 42,
      surveyProcess = SurveyProcess(
        phase = SurveyProcess.Phase.PERSISTED,
        notifications = mutableListOf()
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
