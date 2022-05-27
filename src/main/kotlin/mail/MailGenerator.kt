/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.documents.SurveyPdfDocumentsLinkGenerator
import de.dkjs.survey.model.Project
import de.dkjs.survey.model.SurveyType
import de.dkjs.survey.time.dkjsDate
import de.dkjs.survey.typeform.link.TypeformSurveyLinkGenerator
import org.springframework.stereotype.Component
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component
class MailGenerator @Inject constructor(
  private val typeformSurveyLinkGenerator: TypeformSurveyLinkGenerator,
  private val surveyPdfDocumentsLinkGenerator: SurveyPdfDocumentsLinkGenerator,
  private val templateEngine: SpringTemplateEngine
) {

  fun generate(
    project: Project,
    mailType: MailType,
    surveyType: SurveyType
  ): MailData {
    val formLink = typeformSurveyLinkGenerator.generate(project, surveyType)
    val pdfLink = surveyPdfDocumentsLinkGenerator.generate(project, surveyType)

    val ctx = Context().apply {
      setVariable("projectName", project.name)
      setVariable("projectNumber", project.id)
      setVariable("startDate", project.start.dkjsDate)
      setVariable("endDate", project.end.dkjsDate)
      setVariable("formLink", formLink)
      setVariable("pdfLink", pdfLink)
    }

    val templatePath = "mail/${mailType.name.lowercase()}"

    val bodyHTML = templateEngine.process("$templatePath/body.html", ctx)
    val subject = templateEngine.process("$templatePath/subject.txt", ctx)

    return MailData(subject, bodyHTML)
  }

}
