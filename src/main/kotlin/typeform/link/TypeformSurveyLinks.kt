/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.link

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.Scenario
import de.dkjs.survey.typeform.TypeformConfig
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component
class TypeformSurveyLinkGenerator(
  @Inject private val config: TypeformConfig
) {

  fun generate(
    project: Project,
    scenario: Scenario
  ) = "${config.urlBase}${config.forms.getFormId(project, scenario)}" +
      "#project_id=${project.id}" +
      if (project.isGoalG) "" else "&" + toUrlBlocks(project.goals)

}

fun toUrlBlocks(goals: Set<Int>): String = mapGoalsToBlocks(goals)
  .map { entry -> "${entry.key}=${entry.value}" }
  .joinToString("&")

fun mapGoalsToBlocks(goals: Set<Int>): Map<String, String> = goals
  .sorted()
  .filter { it != 1 }
  .mapIndexed { index, i ->
    Pair(
      "block${index + 2}",
      Character.toString(i + 96)
    )
  }
  .toMap()
