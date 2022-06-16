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

package de.dkjs.survey.rest

import de.dkjs.survey.model.Activity
import de.dkjs.survey.model.Project
import de.dkjs.survey.model.Provider
import de.dkjs.survey.model.SurveyProcess
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry

@Configuration
class RestApiConfiguration {

  @Bean
  fun repositoryRestConfigurer(): RepositoryRestConfigurer =
    RepositoryRestConfigurer.withConfig { configuration: RepositoryRestConfiguration, _: CorsRegistry ->
      configuration.setBasePath("/api")
      configuration.exposeIdsFor(
        Project::class.java,
        Provider::class.java,
        SurveyProcess::class.java,
        Activity::class.java
      )
    }

}
