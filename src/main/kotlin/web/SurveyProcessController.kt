/*
 * dkjs-survey-process-engine - https://www.dkjs.de/
 * Copyright (C) 2022 Kazimierz Pogoda / https://xemantic.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.dkjs.survey.web

import de.dkjs.survey.csv.Column
import de.dkjs.survey.csv.CsvParsingException
import de.dkjs.survey.csv.ProjectCsvParser
import de.dkjs.survey.csv.RowResult
import de.dkjs.survey.engine.DkjsSurveyProcessEngine
import de.dkjs.survey.model.Project
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime
import javax.inject.Inject
import javax.servlet.http.HttpSession

@Controller
class SurveyProcessController @Inject constructor(
  private val logger: Logger,
  private val parser: ProjectCsvParser,
  private val engine: DkjsSurveyProcessEngine
) {

  @Suppress("unused")
  @ModelAttribute
  fun addAttributes(ubc: UriComponentsBuilder, model: Model) {
    val url = ubc.path("/api/explorer/index.html#uri=/api").build().toUriString()
    model.addAttribute("searchUrl", url)
  }

  @Suppress("unused")
  @GetMapping
  fun index() = "index"

  @Suppress("unused")
  @PostMapping("/upload-projects")
  fun uploadProjects(
    @RequestPart("projectsCsv") projectCsv: MultipartFile,
    attributes: RedirectAttributes,
    httpSession: HttpSession
  ): String {

    logger.info("Uploading projects from file: ${projectCsv.originalFilename}")

    val session = SessionView(httpSession)
    session.cleanDkjsAttributes()

    if (projectCsv.isEmpty) {
      attributes.addFlashAttribute(
        "message",
        "Please select a file to upload."
      )
    } else {
      if (projectCsv.originalFilename!!.endsWith(".csv")) {
        try {
          val projects = parser.parse(projectCsv)
          session.projects = projects
        } catch (e: CsvParsingException) {
          logger.error("Error parsing CSV file '${projectCsv.name}': ${e.message}")
          with (attributes) {
            addFlashAttribute("fileName", projectCsv.originalFilename)
            addFlashAttribute("csvError", e.message)
            addFlashAttribute("csvRows", getCsvRows(e))
            addFlashAttribute("csvColumns", Column.values())
            addFlashAttribute("hasErrors", true)
          }
          session.hasErrors = true
        }
      } else {
        with (attributes) {
          addFlashAttribute("fileName", projectCsv.originalFilename)
          addFlashAttribute(
            "message",
            "Only CSV files can be uploaded."
          )
        }
      }
    }

    return "redirect:/"
  }

  @Suppress("unused")
  @GetMapping("/submit-projects")
  fun submitProjects(httpSession: HttpSession): ResponseEntity<String> {

    logger.info("Submitting projects from HTTP session")

    val session = SessionView(httpSession)
    return if (session.hasErrors) { // should never happen in typical workflows
      logger.error("An attempt to submit projects with errors")
      ResponseEntity.internalServerError().body("Cannot submit projects with errors")
    } else {
      val projects = session.projects
      if (projects != null && !projects.isEmpty()) {
        logger.info("Submitted project count: ${projects.size}")
        engine.handleProjects(projects, LocalDateTime.now())
        session.cleanDkjsAttributes()
        ResponseEntity.ok("Projects submitted")
      } else { // should never happen in typical workflows
        logger.error("An attempt to submit projects, but none stored in HTTP session")
        ResponseEntity.internalServerError().body("No projects found to submit")
      }
    }
  }

}

class SessionView(private val session: HttpSession) {

  var hasErrors: Boolean
    get() = session.attribute("hasErrors") ?: false
    set(value) = session.setAttribute("hasErrors", value)

  var projects: List<Project>?
    get() = session.attribute("projects")
    set(value) = session.setAttribute("projects", value)

  fun cleanDkjsAttributes() {
    session.removeAttribute("projects")
    session.removeAttribute("hasErrors")
  }

}

private inline fun <reified T> HttpSession.attribute(name: String): T = this.getAttribute(name) as T

data class CsvRow(
  val data: List<String>,
  val rowErrors: List<String>,
  val columnErrors: Map<Column, List<String>>
) {
  @Suppress("unused") // used in HTML template
  val hasErrors: Boolean get() = rowErrors.isNotEmpty() || columnErrors.isNotEmpty()
}

private fun getCsvRows(e: CsvParsingException): List<CsvRow> = e.rows.map {
  CsvRow(
    it.csvRow,
    it.errors
      .filterIsInstance<RowResult.RowError>()
      .map { it.message },
    it.errors
      .filterIsInstance<RowResult.ColumnError>()
      .groupBy { it.column }
      .map { Pair(it.key, it.value.map { it.message }) }
      .toMap()
  )
}
