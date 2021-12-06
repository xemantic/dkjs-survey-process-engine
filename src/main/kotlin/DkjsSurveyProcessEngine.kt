package de.dkjs.survey

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class DkjsSurveyProcessEngine

fun main(vararg args: String) {

  runApplication<DkjsSurveyProcessEngine>(*args)

}
