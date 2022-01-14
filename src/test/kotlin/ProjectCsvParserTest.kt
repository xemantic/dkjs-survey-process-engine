/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.model.*
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.core.io.InputStreamSource
import java.io.File
import java.time.LocalDate

class ProjectCsvParserTest {
  private val repository = mockk<ProjectRepository>(relaxed = true)
  private fun csvToProjects(path: String): List<ProjectParsingResult> {
    val parser = ProjectCsvParser(repository)
    val file = InputStreamSource { File("src/test/sheets/$path").inputStream() }
    return parser.parse(file)
  }

  @Test
  fun `should parse project CSV file with single entry`() {
    // given
    val results = csvToProjects("single.csv")
    // when

    // then
    results shouldHaveSize 1
    val result = results[0]
    result.message shouldBe null
    result.project shouldNotBe null
    val project = result.project!!
    project.name shouldBe "expectedProjectName"
  }

  @Test
  fun `should not parse broken CSV`() {
    // given
    val results = csvToProjects("broken.csv")
    // when

    // then
    results.shouldBeEmpty()
  }

  @Test
  fun `should parse project CSV file with multiple entries`() {
    // given
    val results = csvToProjects("multiple.csv")
    // when

    // then
    results shouldHaveSize 5
    results.forEachIndexed { i, result ->
      result.message shouldBe null
      result.project!!.name shouldBe "project${i + 1}"
    }
  }

  @Test
  fun `should report error if project is specified twice`() {
    // given
    val results = csvToProjects("repeatedProject.csv")
    // when

    // then
    results shouldHaveSize 5
    results[3].message shouldContain "CSV parsing error: duplicate project number"
  }

  @Test
  fun `should report error if CSV file contains incorrect data`() {
    // given
    val results = csvToProjects("invalidData.csv")
    // when

    // then
    results shouldHaveSize 6
    results[0].message shouldBe "CSV parsing error: invalid e-mail"
    results[1].message shouldBe "CSV parsing error: invalid start date"
    results[2].message shouldBe "CSV parsing error: invalid end date"
    results[3].message shouldBe "CSV parsing error: invalid worker"
    results[4].message shouldBe "CSV parsing error: invalid age1to5"
    results[5].message shouldContain "CSV parsing error: wrong column count"
  }

  @Test
  fun `should report error if project already exists`() {
    // given
    every { repository.existsById("4021000014 -1") } answers { true }

    // when
    val results = csvToProjects("single.csv")

    // then
    results shouldHaveSize 1
    val result = results[0]
    result.message shouldContain "already exists"
    result.project shouldBe null
  }
}
