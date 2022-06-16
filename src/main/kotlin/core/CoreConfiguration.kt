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
