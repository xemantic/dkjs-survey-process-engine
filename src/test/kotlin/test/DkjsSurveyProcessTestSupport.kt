/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.test

import com.ninjasquad.springmockk.MockkBean
import de.dkjs.survey.csv.Column
import de.dkjs.survey.csv.CsvParsingException
import de.dkjs.survey.csv.RowResult
import de.dkjs.survey.engine.AlertSender
import de.dkjs.survey.mail.SurveyEmailSender
import de.dkjs.survey.model.*
import de.dkjs.survey.typeform.response.TypeformResponseChecker
import org.slf4j.Logger
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.justRun
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime

// utilities useful in testing


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

fun SurveyProcess.addTestActivity(name: String) {
  this.activities.add(
    Activity(
      surveyProcessId = "foo",
      name = name,
      result = "test ok"
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

const val TEST_PROCESS_TIMEOUT = 60000

const val PROCESS_STATE_POLLING_INTERVAL = 200L

@DkjsSurveyProcessEngineTest
abstract class SurveyProcessTestBase {

  @Autowired
  lateinit var logger: Logger

  @Autowired
  lateinit var processRepository: SurveyProcessRepository

  @Autowired
  lateinit var client: WebTestClient

  @MockkBean
  lateinit var surveyEmailSender: SurveyEmailSender

  @MockkBean
  lateinit var alertSender: AlertSender

  @MockkBean
  lateinit var typeformResponseChecker: TypeformResponseChecker

  fun now(): LocalDateTime = LocalDateTime.now()

  fun uploadingProjectsCsv(csv: String) {

    var jSessionId: String? = null

    client.post()
      .uri("/upload-projects")
      .body(
        BodyInserters.fromMultipartData(MultipartBodyBuilder().also {
          it.part("projectsCsv", csv.trimIndent().trim().toByteArray())
            .header("Content-Disposition", "form-data; name=projectsCsv; filename=projects.csv")
        }.build())
      )
      .exchange()
      .expectStatus().is3xxRedirection
      .expectCookie().value("JSESSIONID") {
        jSessionId = it!!
      }

    client.get()
      .uri("/submit-projects")
      .cookie("JSESSIONID", jSessionId!!)
      .exchange()
      .expectStatus().is2xxSuccessful

  }

  private fun processIsActive(processId: String) =
    processRepository.findByIdOrNull(processId)!!.phase == SurveyProcess.Phase.ACTIVE

  fun waitingUntilProcessEnds(processId: String) {
    val start = System.currentTimeMillis()
    var timeout: Boolean
    do {
      Thread.sleep(PROCESS_STATE_POLLING_INTERVAL)
      timeout = (System.currentTimeMillis() - start) >= TEST_PROCESS_TIMEOUT
    } while (processIsActive(processId) && !timeout)
    if (timeout) {
      fail("A timeout when waiting for the process to end")
    }
  }

  fun numberOfFilledSurveys(count: Int) {
    every { typeformResponseChecker.countSurveys(any(), any()) } returns count
  }

  @BeforeEach
  fun beforeTest() {
    justRun { surveyEmailSender.send(any(), any(), any()) }
    justRun { alertSender.sendProcessAlert(any(), any(), any()) }
  }

  @AfterEach
  fun afterTest() {
    logger.info("Confirming verification of mocked SurveyEmailSender and AlertSender")
    confirmVerified(
      surveyEmailSender,
      alertSender
    )
  }

}
