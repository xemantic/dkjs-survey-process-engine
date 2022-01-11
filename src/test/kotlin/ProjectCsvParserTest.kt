/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import org.junit.jupiter.api.Test
import java.io.File

class ProjectCsvParserTest {

  @Test
  fun `should parse project CSV file`() {
    // given
    val parser = ProjectCsvParser()
    val file = File("src/test/sheets/applicationdata_2022-06-01.csv")

    // when
    val result = file.inputStream().use {
      parser.parse(it)
    }

    // then
    result
  }

}
