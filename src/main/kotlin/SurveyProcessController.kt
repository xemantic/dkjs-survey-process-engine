/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.inject.Inject


@RestController
@Validated
open class SurveyProcessController @Inject constructor(
  private val parser: ProjectCsvParser,
  private val engine: DkjsSurveyProcessEngine
) {

  // the controller does nothing at the moment, should support CSV file upload and parsing

  @PostMapping("/upload-projects")
  fun uploadProjects(
    @RequestParam("projects") projectCsv: MultipartFile,
    redirectAttributes: RedirectAttributes
  ): String {

    val result = parser.parse(projectCsv)

    // TODO render result in case of errors

    redirectAttributes.addFlashAttribute(
      "message",
      "You successfully uploaded " + projectCsv.originalFilename + "!"
    )
    return "redirect:/"
  }

}
