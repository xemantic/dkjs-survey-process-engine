/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.csv.Column
import de.dkjs.survey.csv.CsvParsingException
import de.dkjs.survey.csv.ProjectCsvParser
import de.dkjs.survey.csv.RowResult
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
  private val engine: DkjsSurveyProcessEngine
) {

//  @GetMapping()
//  fun index() = "index"

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
    } else {
      if (projectCsv.originalFilename!!.endsWith(".csv")) {
        try {
          val projects = parser.parse(projectCsv)
          session.setAttribute("projects", projects)
        } catch (e: CsvParsingException) {
          logger.error("Error parsing CSV file '${projectCsv.name}': ${e.message}")
          with (attributes) {
            addFlashAttribute("fileName", projectCsv.originalFilename)
            addFlashAttribute("csvError", e.message)
            addFlashAttribute("csvRows", getCsvRows(e))
            addFlashAttribute("csvColumns", Column.values())
            addFlashAttribute("hasErrors", true)
          }
          session.setAttribute("hasErrors", true)
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
}

private fun HttpSession.cleanDkjsAttributes() {
  this.removeAttribute("projects")
  this.removeAttribute("hasErrors")
}

data class CsvRow(
  val data: List<String>,
  val rowErrors: List<String>,
  val columnErrors: Map<Column, List<String>>
) {
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
