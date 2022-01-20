/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.test

import org.slf4j.Logger
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.concurrent.TimeUnit
import de.dkjs.survey.util.debug

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
