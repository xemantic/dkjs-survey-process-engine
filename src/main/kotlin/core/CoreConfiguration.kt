/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.core.Ordered
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.filter.ForwardedHeaderFilter
import javax.servlet.DispatcherType


@Configuration
class CoreConfiguration {

  @Scope("prototype")
  @Bean
  fun logger(injectionPoint: InjectionPoint): Logger = LoggerFactory.getLogger(
    injectionPoint.methodParameter?.containingClass // constructor
      ?: injectionPoint.field?.declaringClass // or field injection
  )

  @Bean
  fun taskScheduler(): ThreadPoolTaskScheduler = ThreadPoolTaskScheduler().apply {
    setAwaitTerminationSeconds(30)
    setWaitForTasksToCompleteOnShutdown(true)
    setContinueExistingPeriodicTasksAfterShutdownPolicy(false)
    setThreadNamePrefix("process")
    poolSize = 1
  }

  @Bean
  fun forwardedHeaderFilter(): FilterRegistrationBean<ForwardedHeaderFilter> {
    val filter = ForwardedHeaderFilter()
    val registration: FilterRegistrationBean<ForwardedHeaderFilter> =
      FilterRegistrationBean<ForwardedHeaderFilter>(filter)
    registration.setDispatcherTypes(
      DispatcherType.REQUEST,
      DispatcherType.ASYNC,
      DispatcherType.ERROR
    )
    registration.order = Ordered.HIGHEST_PRECEDENCE
    return registration
  }

}
