/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.link

import de.dkjs.survey.model.ScenarioType
import de.dkjs.survey.typeform.TypeformConfig
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

@Component
@Singleton
class TypeformSurveyLinkGenerator(
  @Inject private val config: TypeformConfig
) {

  fun generate(
    projectId: String,
    goals: Set<Int>,
    scenarioType: ScenarioType
  ) = "${config.linkBase}${config.forms.getFormId(scenarioType)}" +
      "?project_id=$projectId&${toUrlBlocks(goals)}"

}

fun toUrlBlocks(goals: Set<Int>): String = mapGoalsToBlocks(goals)
  .map { entry -> "${entry.key}=${entry.value}" }
  .joinToString("&")

fun mapGoalsToBlocks(goals: Set<Int>): Map<String, String> = goals
  .filter { it != 1 }
  .mapIndexed { index, i ->
    Pair(
      "block${index + 2}",
      Character.toString(i + 96)
    )
  }
  .toMap()
