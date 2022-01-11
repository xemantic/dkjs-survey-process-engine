/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.core.io.InputStreamSource
import java.io.File

class ProjectCsvParserTest {

  @Test
  fun `should parse project CSV file with single entry`() {
    // given
    val parser = ProjectCsvParser()
    val file = InputStreamSource {
      File("src/test/sheets/applicationdata_2022-06-01.csv").inputStream()
    }

    // when
    val result = parser.parse(file)

    // then
    result shouldHaveSize 1
    val entry = result[0]
    entry.message shouldBe null
    entry.project shouldNotBe null
    val project = entry.project!!
    project.name shouldBe "Foo"
  }

  @Test
  fun `should not parse broken CSV`() {

    // then one list element should be returned with only a message

  }

  @Test
  fun `should parse project CSV file with multiple entries`() {

  }

  @Test
  fun `should report error if project already exists`() {

  }

  @Test
  fun `should report error if project is specified twice`() {

  }

}
