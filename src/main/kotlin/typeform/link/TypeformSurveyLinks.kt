/*
 * dkjs-survey-process-engine - https://www.dkjs.de/
 * Copyright (C) 2022 Kazimierz Pogoda / https://xemantic.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.dkjs.survey.typeform.link

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.SurveyType
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
    surveyType: SurveyType
  ) = "${config.urlBase}${config.forms.getFormId(project, surveyType)}" +
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
