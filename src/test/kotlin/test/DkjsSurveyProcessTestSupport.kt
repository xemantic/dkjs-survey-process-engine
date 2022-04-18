/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.test

import de.dkjs.survey.csv.Column
import de.dkjs.survey.csv.CsvParsingException
import de.dkjs.survey.csv.RowResult
import de.dkjs.survey.mail.MailType
import de.dkjs.survey.model.*
import org.slf4j.Logger
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.concurrent.TimeUnit
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactly
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
  logger.info("Sleeping until maximal possible project duration is reached: $seconds seconds")
  TimeUnit.SECONDS.sleep(seconds.toLong())
}

fun startOfDay(year: Int, month: Int, day: Int): LocalDateTime =
  LocalDate.of(year, month, day).atStartOfDay()

fun CsvParsingException.shouldReportRow(row: Int, vararg errors: RowResult.Error) {
  require(row > 0) { "row numeration must start with 1" }
  require(errors.isNotEmpty()) { "messages cannot be empty" }
  if (errors.size == 1) {
    rows[row - 1].errors[0] shouldBe errors[0]
  } else {
    assertSoftly {
      rows shouldHaveAtLeastSize row
      rows[row - 1].errors
        .sortedWith(errorOrder)
        .shouldContainExactly(
          errors.sortedWith(errorOrder).toList()
        )
    }
  }
}

private val errorOrder = Comparator<RowResult.Error> { a, b ->
  val difference = if ((a is RowResult.ColumnError) && (b is RowResult.ColumnError)) {
    a.column.compareTo(b.column)
  } else 0
  if (difference != 0) difference
  else a.message.compareTo(b.message)
}

fun CsvParsingException.shouldNotReportRow(row: Int) {
  require(row > 0) { "row numeration must start with 1" }
  rows shouldHaveAtLeastSize row
  rows[row - 1].errors shouldHaveSize 0
}

infix fun Column.that(message: String): RowResult.ColumnError = RowResult.ColumnError(
  message,
  this
)

private var notificationSequence: Int = 0

fun SurveyProcess.addNotification(mailType: MailType) {
  this.notifications.add(
    Notification(
      id = notificationSequence++,
      surveyProcessId = this.id,
      mailType = mailType,
      sentAt = LocalDateTime.now()
    )
  )
}

fun projectWithGoals(id: String, vararg goals: Int) = Project(
  id          = id,
  status      = "",
  name        = "",
  provider = Provider(
    id        = "",
    name      = ""
  ),
  contactPerson = ContactPerson(
    pronoun   = "",
    firstName = "",
    lastName  = "",
    email     = ""
  ),
  goals       = goals.toSet(),
  participants = Participants(
    age1to5   = -1,
    age6to10  = -1,
    age11to15 = -1,
    age16to19 = -1,
    age20to26 = -1,
    worker    = -1
  ),
  start       = LocalDateTime.MIN,
  end         = LocalDateTime.MIN
)
