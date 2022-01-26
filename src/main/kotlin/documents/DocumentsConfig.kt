/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.documents

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@ConstructorBinding
@ConfigurationProperties("documents")
@Validated
data class DocumentsConfig(

  @NotEmpty
  val linkBase: String

)
