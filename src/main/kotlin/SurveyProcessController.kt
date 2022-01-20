/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.inject.Inject


@RestController
open class SurveyProcessController @Inject constructor(
  private val parser: ProjectCsvParser,
  private val engine: DkjsSurveyProcessEngine
) {

  // the controller does nothing at the moment, should support CSV file upload and parsing

  @PostMapping("/upload-projects")
  fun uploadProjects(
    @RequestPart("projectsCsv") projectCsv: MultipartFile
  ): String {

    val result = projectCsv.inputStream.use {
      parser.parse(it)
    }

    // TODO render result in case of errors

//    redirectAttributes.addFlashAttribute(
//      "message",
//      "You successfully uploaded " + projectCsv.originalFilename + "!"
//    )
    return "redirect:/"
  }

}
