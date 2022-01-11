/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import de.dkjs.survey.model.Project
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Component
import java.io.InputStreamReader
import javax.inject.Singleton


@Singleton
@Component
// TODO we will need to inject ProjectRepository
class ProjectCsvParser {

  fun parse(projectCsv: InputStreamSource): List<ProjectParsingResult> =
    CSVReaderBuilder(
      InputStreamReader(projectCsv.inputStream, "ISO_8859-15")
    ).withCSVParser(
        CSVParserBuilder()
          .withSeparator(';')
          .build()
      ).build().use { csvReader ->
        csvReader.readAll()
      }.map { row ->
        // TODO actual parsing
        ProjectParsingResult(null, null)
      }


}

// either project or a message when it cannot be parsed
class ProjectParsingResult(
  val project: Project?,
  val message: String?
)
