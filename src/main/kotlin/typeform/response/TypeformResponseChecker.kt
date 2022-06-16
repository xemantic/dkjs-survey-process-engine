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

package de.dkjs.survey.typeform.response

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.SurveyType
import de.dkjs.survey.typeform.TypeformConfig
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component
class TypeformResponseChecker @Inject constructor(
  private val service: TypeformResponseService,
  private val config: TypeformConfig
) {

  fun countSurveys(project: Project, surveyType: SurveyType): Int = runBlocking {
    service.countResponses(
      config.forms.getFormId(project, surveyType),
      project.id
    )
  }

}
