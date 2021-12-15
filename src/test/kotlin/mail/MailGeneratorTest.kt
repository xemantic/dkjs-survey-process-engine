/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.ProcessPhase
import de.dkjs.survey.model.SurveyProcess
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class MailGeneratorTest {

  @Test
  fun shouldGenerateMailFromTemplate() {
    // given
    val now = LocalDate.now()
    val process = SurveyProcess(
      projectName = "Foo",
      projectNumber = "42",
      projectContact = "Herr Max Mustermann",
      startDate = parseDate("20220115"),
      endDate = parseDate("20220130"),
      //goals = setOf(Goal.A),
      email = "max@musterman.de",
      processPhase = ProcessPhase.READY_TO_NOTIFY_THE_PROJECT,
    )
    val templates = EnumMap<MailTemplate, MailTemplateData>(MailTemplate::class.java)
    templates[MailTemplate.INFOMAIL_PRE_POST] = MailTemplateData(
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
    val mail = mailGenerator.generate(MailTemplate.INFOMAIL_PRE_POST, process)

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
