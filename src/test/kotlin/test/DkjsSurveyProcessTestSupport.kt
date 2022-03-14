/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.test

import de.dkjs.survey.csv.CsvParsingException
import org.slf4j.Logger
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.concurrent.TimeUnit
import de.dkjs.survey.util.debug
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime

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

fun startOfDay(year: Int, month: Int, day: Int): LocalDateTime =
  LocalDate.of(year, month, day).atStartOfDay()

fun CsvParsingException.shouldReportRow(row: Int, vararg messages: String) {
  require(row > 0) { "row numeration must start with 1" }
  require(messages.isNotEmpty()) { "messages cannot be empty" }
  if (messages.size == 1) {
    rows[row - 1].messages[0] shouldBe messages[0]
  } else {
    assertSoftly {
      rows shouldHaveAtLeastSize row
      rows[row - 1].messages shouldContainExactlyInAnyOrder messages.toList()
    }
  }
}

infix fun CsvParsingException.shouldReportFirstRow(message: String) = assertSoftly {
  rows shouldHaveSize 1
  rows[0].messages shouldHaveSize 1
  rows[0].messages[0] shouldBe message
}
