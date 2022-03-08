/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.documents

import de.dkjs.survey.model.Project
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotEmpty

@ConstructorBinding
@ConfigurationProperties("documents")
@Validated
data class DocumentsConfig(

  @NotEmpty
  val linkBase: String,

  @NotEmpty
  val pdfURL: String

)

@Component
@Singleton
class SurveyDocumentPdfLinkGenerator(
  @Inject private val config: DocumentsConfig
) {

  // TODO we need a rule for generating these
  fun generate(project: Project) =
    config.pdfURL.format(config.linkBase, "TODO")

}
