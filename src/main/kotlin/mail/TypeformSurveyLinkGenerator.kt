/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.documents.DocumentsConfig
import de.dkjs.survey.model.Project
import de.dkjs.survey.typeform.TypeformConfig
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

@Component
@Singleton
class TypeformSurveyLinkGenerator(
  @Inject private val config: TypeformConfig
) {

  fun generate(project: Project) =
    "${config.linkBase}?projectNumber=${project.id}&blocks=${project.name}"

}

@Component
@Singleton
class SurveyDocumentPdfLinkGenerator(
  @Inject private val config: DocumentsConfig
) {

  // TODO we need a rule for generating these
  fun generate(project: Project) = config.linkBase

}
