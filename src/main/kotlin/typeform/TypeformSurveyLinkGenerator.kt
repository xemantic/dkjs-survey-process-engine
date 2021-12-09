/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class TypeformSurveyLinkGenerator @Inject constructor(
  @Value("\${typeform.linkBase}") private val typeformLinkBase: String,
) {

  fun generate(projectId: String, blocks: String) =
    "$typeformLinkBase?projectId=$projectId&blocks=$blocks"

}
