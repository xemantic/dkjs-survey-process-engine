/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.mail.EmailSenderService
import de.dkjs.survey.model.Project
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.inject.Inject
import javax.servlet.http.HttpSession

@Controller
class SurveyProcessController @Inject constructor(
  private val logger: Logger,
  private val parser: ProjectCsvParser,
  private val engine: DkjsSurveyProcessEngine,
  private val emailSenderService: EmailSenderService // TODO Delete (EML_TEST)
) {

//  @GetMapping()
//  fun index() = "index"

  // the controller does nothing at the moment, should support CSV file upload and parsing

  @PostMapping("/upload-projects")
  fun uploadProjects(
    @RequestPart("projectsCsv") projectCsv: MultipartFile,
    attributes: RedirectAttributes,
    session: HttpSession
  ): String {

    session.cleanDkjsAttributes()

    if (projectCsv.isEmpty) {
      attributes.addFlashAttribute(
        "message",
        "Please select a file to upload."
      )
    }

    try {
      val projects = parser.parse(projectCsv)
      session.setAttribute("projects", projects)
    } catch (e: CsvParsingException) {
      logger.error("Error parsing CSV file: ${projectCsv.name}", e)
      attributes.addFlashAttribute("errors", e.rows)
      session.setAttribute("hasErrors", true)
    }

    return "redirect:/"
  }

  // TODO Delete (EML_TEST)
  @GetMapping("/sendmail")
  fun mail(): String {
    emailSenderService.send()
    return "redirect:/"
  }

  @GetMapping("/submit-projects")
  fun submitProjects(
    session: HttpSession
  ): String {
    @Suppress("UNCHECKED_CAST")
    val hasErrors = session.getAttribute("hasErrors") as Boolean?
    if (hasErrors != null && hasErrors) {
      logger.error("An attempt to submit projects with errors")
      return "redirect:/"
    }
    @Suppress("UNCHECKED_CAST")
    val projects: List<Project> = session.getAttribute("projects") as List<Project>
    projects.forEach {
      engine.handleNew(it)
    }
    session.cleanDkjsAttributes()
    return "redirect:/"
  }

  private fun HttpSession.cleanDkjsAttributes() {
    this.removeAttribute("projects")
    this.removeAttribute("hasErrors")
  }

}
