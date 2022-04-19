/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

// security is enabled by security profile
@SpringBootApplication(
  exclude = [ SecurityAutoConfiguration::class ]
)
@ConfigurationPropertiesScan
class DkjsSurveyProcessApplication

fun main(vararg args: String) {
  runApplication<DkjsSurveyProcessApplication>(*args)
}
