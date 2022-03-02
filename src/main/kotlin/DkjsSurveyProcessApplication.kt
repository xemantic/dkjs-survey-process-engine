/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.Provider
import de.dkjs.survey.model.SurveyProcess
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.*
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.web.servlet.config.annotation.CorsRegistry
import javax.inject.Singleton

@SpringBootApplication
@ConfigurationPropertiesScan
class DkjsSurveyProcessApplication {

  @Scope("prototype")
  @Bean
  fun logger(injectionPoint: InjectionPoint): Logger = LoggerFactory.getLogger(
    injectionPoint.methodParameter?.containingClass // constructor
      ?: injectionPoint.field?.declaringClass // or field injection
  )

  @Singleton
  @Bean
  fun taskScheduler(): TaskScheduler = ConcurrentTaskScheduler() //single threaded by default

  @Bean
  fun repositoryRestConfigurer(): RepositoryRestConfigurer {
    return RepositoryRestConfigurer.withConfig { configuration: RepositoryRestConfiguration, _: CorsRegistry ->
      configuration.setBasePath("/api")
      configuration.exposeIdsFor(
        Project::class.java,
        Provider::class.java,
        SurveyProcess::class.java
      )
    }
  }

}

fun main(vararg args: String) {

  runApplication<DkjsSurveyProcessApplication>(*args)

}
