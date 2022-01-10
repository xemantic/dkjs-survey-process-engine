/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.Project
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

@Component
@Singleton
class TypeformSurveyLinkGenerator @Inject constructor(
  @Value("\${typeform.linkBase}") private val typeformLinkBase: String,
) {

  fun generate(project: Project) =
    "$typeformLinkBase?projectNumber=${project.id}&blocks=${project.name}"

}

@Component
@Singleton
class PdfSurveyLinkGenerator @Inject constructor(
  @Value("\${pdf.linkBase}") private val pdfLinkBase: String,
) {

  // TODO we need a rule for generating these
  fun generate(project: Project) = pdfLinkBase

}
