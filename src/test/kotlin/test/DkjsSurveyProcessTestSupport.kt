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

package de.dkjs.survey.test

import com.ninjasquad.springmockk.MockkBean
import de.dkjs.survey.csv.Column
import de.dkjs.survey.csv.CsvParsingException
import de.dkjs.survey.csv.ProjectCsvParser
import de.dkjs.survey.csv.RowResult
import de.dkjs.survey.engine.AlertSender
import de.dkjs.survey.engine.DkjsSurveyProcessEngine
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
import java.io.ByteArrayInputStream
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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

fun projectWithGoals(goals: List<Int>): Project = projectWithGoals(
  id = "foo",
  goals = goals
)

fun projectWithGoals(id: String, vararg goals: Int): Project =
  projectWithGoals(id, goals.toList())

fun projectWithGoals(id: String, goals: List<Int>): Project =
  Project(
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

fun ProjectCsvParser.parse(csv: String): List<Project> = ByteArrayInputStream(
  csv.trimIndent().toByteArray()
).use {
  this.parse { it }
}

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

  @Autowired
  lateinit var engine: DkjsSurveyProcessEngine

  @Autowired
  lateinit var parser: ProjectCsvParser

  @MockkBean
  lateinit var surveyEmailSender: SurveyEmailSender

  @MockkBean
  lateinit var alertSender: AlertSender

  @MockkBean
  lateinit var typeformResponseChecker: TypeformResponseChecker

  fun now(): LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

  val Int.days: Duration get() = Duration.ofSeconds(this.toLong())

  fun uploadingProjectsCsv(csv: String, processStartTime: LocalDateTime) {
    val projects = parser.parse(csv)
    engine.handleProjects(projects, processStartTime)
  }

  fun uploadingProjectsCsvThroughHttp(csv: String) {

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

  fun numberOfSurveyResponses(surveyType: SurveyType, count: Int) {
    every { typeformResponseChecker.countSurveys(any(), surveyType) } returns count
  }

  @BeforeEach
  fun beforeTest() {
    justRun { surveyEmailSender.send(any(), any(), any()) }
    justRun { alertSender.sendProcessAlert(any(), any()) }
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
