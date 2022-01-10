/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.model.Project
import org.springframework.stereotype.Component
import java.io.InputStream
import javax.inject.Singleton

@Singleton
@Component
class ProjectCsvParser {

  fun parse(input: InputStream): ProjectParsingResult {
    // TODO provide implementation using opencsv
    return ProjectParsingResult(null, null)
  }

}

// either project or a message when it cannot be parsed
class ProjectParsingResult(
  val project: Project?,
  val message: String?
)
