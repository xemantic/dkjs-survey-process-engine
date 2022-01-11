/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.Project
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

@Component
class MailGenerator @Inject constructor(
  @Named("templates") private val templates: Map<MailType, MailTemplateData>,
  private val typeformSurveyLinkGenerator: TypeformSurveyLinkGenerator,
  private val pdfSurveyLinkGenerator: PdfSurveyLinkGenerator
) {

  private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

  fun generate(
    mailType: MailType,
    project: Project
  ): MailData {
    val templateData = templates[mailType]!!
    val typeformLink = typeformSurveyLinkGenerator.generate(project)
    val pdfLink = pdfSurveyLinkGenerator.generate(project)
    val subject = replaceTokens(templateData.subject, project)
    val body = replaceTokens(templateData.body, project, typeformLink, pdfLink)
    return MailData(subject, body)
  }

  // TODO this should be completely adjusted now after model is stable
  fun replaceTokens(
    input: String,
    project: Project,
    typeformLink: String = "",
    pdfLink: String = ""
  ) = input
    .replace("{projectContact}",  project.contactPerson.firstName)
    .replace("{projectName}",     project.name)
    .replace("{projectNumber}",   project.id)
    .replace("{startDate}",       project.start.format(dateFormatter))
    .replace("{endDate}",         project.end.format(dateFormatter))
    .replace("{typeformLink}",    typeformLink)
    .replace("{pdfLink}",         pdfLink)

}
