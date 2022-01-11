/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import de.dkjs.survey.model.Project
import org.springframework.stereotype.Component
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Singleton


@Singleton
@Component
class ProjectCsvParser {

  fun parse(input: InputStream): List<ProjectParsingResult> {
    CSVReaderBuilder(InputStreamReader(input, "ISO_8859-15"))
      .withCSVParser(
        CSVParserBuilder()
          .withSeparator(';')
          .build()
      ).build().use { csvReader ->
      val list = csvReader.readAll().forEach {
        println(it.joinToString("|"))
      }
    }

    // TODO provide implementation using opencsv
    return listOf(ProjectParsingResult(null, null))
  }

}

// either project or a message when it cannot be parsed
class ProjectParsingResult(
  val project: Project?,
  val message: String?
)
