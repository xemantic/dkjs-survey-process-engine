/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.rest

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
        SurveyProcess::class.java
      )
    }

}
