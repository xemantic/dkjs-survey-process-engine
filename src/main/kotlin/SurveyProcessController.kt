/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import org.slf4j.Logger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.inject.Inject


@RestController
class SurveyProcessController @Inject constructor(
  private val logger: Logger,
  private val parser: ProjectCsvParser,
  private val engine: DkjsSurveyProcessEngine
) {

  // the controller does nothing at the moment, should support CSV file upload and parsing

  @PostMapping("/upload-projects")
  fun uploadProjects(
    @RequestPart("projectsCsv") projectCsv: MultipartFile
  ): String {

    try {
      parser.parse(projectCsv)
    } catch (e: CsvParsingException) {
      logger.error("Error parsing CSV file: ${projectCsv.name}", e)
    }

    // TODO render result in case of errors

//    redirectAttributes.addFlashAttribute(
//      "message",
//      "You successfully uploaded " + projectCsv.originalFilename + "!"
//    )
    return "redirect:/"
  }

}
