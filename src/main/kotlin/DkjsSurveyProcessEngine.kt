/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.mail.javamail.JavaMailSenderImpl
import javax.inject.Singleton

@SpringBootApplication
open class DkjsSurveyProcessEngine {

  @Singleton
  @Bean
  open fun mailSender() = JavaMailSenderImpl()

}

fun main(vararg args: String) {

  runApplication<DkjsSurveyProcessEngine>(*args)

}
