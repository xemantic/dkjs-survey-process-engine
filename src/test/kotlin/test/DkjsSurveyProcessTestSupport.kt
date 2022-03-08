/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.test

import de.dkjs.survey.CsvParsingException
import org.slf4j.Logger
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.concurrent.TimeUnit
import de.dkjs.survey.util.debug
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

// utilities useful in testing

fun WebTestClient.uploadProjectsCsv(csv: String): WebTestClient.ResponseSpec =
  this
    .post()
    .uri("/upload-projects")
    .body(
      BodyInserters.fromMultipartData(MultipartBodyBuilder().also {
        it.part("projectsCsv", csv.trimIndent().trim().toByteArray())
          .header("Content-Disposition", "form-data; name=projectsCsv; filename=projects.csv")
      }.build())
    )
    .exchange()

fun sleepForMaximalProcessDuration(logger: Logger, seconds: Int) {
  logger.debug { "Sleeping until maximal possible project duration is reached: $seconds seconds" }
  TimeUnit.SECONDS.sleep(seconds.toLong())
}

/**
 * Asserts that [CsvParsingException] has one and only one specified message.
 */
infix fun CsvParsingException.shouldReport(message: String) {
  this.rows shouldHaveSize 1
  this.rows.first().messages shouldHaveSize 1
  this.rows.first().messages.first() shouldBe message
}
