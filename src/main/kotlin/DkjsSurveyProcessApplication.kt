/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.*
import org.springframework.mail.MailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import javax.inject.Singleton

@SpringBootApplication
open class DkjsSurveyProcessApplication {

  @Scope("prototype")
  @Bean
  open fun logger(injectionPoint: InjectionPoint): Logger = LoggerFactory.getLogger(
    injectionPoint.methodParameter?.containingClass // constructor
      ?: injectionPoint.field?.declaringClass // or field injection
  )

  @Singleton
  @Bean
  open fun taskScheduler(): TaskScheduler = ConcurrentTaskScheduler() //single threaded by default

  @Singleton
  @Bean
  @Profile("prod") // it will have another implementation in test
  open fun mailSender(): MailSender = JavaMailSenderImpl()

}

fun main(vararg args: String) {

  runApplication<DkjsSurveyProcessApplication>(*args)

}
