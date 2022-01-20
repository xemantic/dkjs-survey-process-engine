/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 * Copyright (c) 2022 Abe Pazos / Xemantic
 */

package de.dkjs.survey

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.opencsv.exceptions.CsvMalformedLineException
import de.dkjs.survey.model.*
import de.dkjs.survey.time.parseDkjsDate
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Component
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

private enum class Column {

  PROJECT_NUMBER,
  PROJECT_STATUS,
  PROJECT_PROVIDER,
  PROVIDER_NUMBER,
  PROJECT_PRONOUN,
  PROJECT_FIRSTNAME,
  PROJECT_LASTNAME,
  PROJECT_MAIL,
  PROJECT_NAME,
  PARTICIPANTS_AGE1TO5,
  PARTICIPANTS_AGE6TO10,
  PARTICIPANTS_AGE11TO15,
  PARTICIPANTS_AGE16TO19,
  PARTICIPANTS_AGE20TO26,
  PARTICIPANTS_WORKER,
  PROJECT_GOALS,
  PROJECT_START,
  PROJECT_END;

  val csvName get() = name.replace('_', '.').lowercase()

}

@Singleton
@Component
class ProjectCsvParser @Inject constructor(
  private val repository: ProjectRepository,
  private val validator: Validator
) {

  /**
   * [ProjectCsvParser] creates temporary [invalidProject]s
   * when lines can not be parsed. When this happens
   * an [CsvParsingException] is thrown after attempting
   * to parse all CSV lines containing details about all parsing issues.
   * The [invalidProject] is never actually returned.
   */
  private fun invalidProject() = Project(
    "invalid-" + System.currentTimeMillis(),
    "invalid",
    "invalid",
    Provider("", ""),
    ContactPerson("", "", "", ""),
    setOf(),
    Participants(0, 0, 0, 0, 0, null),
    LocalDateTime.MIN,
    LocalDateTime.MIN,
    null
  )



  /**
   * Parses a CSV file creating a list of items, each item containing
   * a [Project] and null `message` on success, or
   * a null [Project] and a `message` describing why it couldn't be created.
   */
  fun parse(projectCsv: InputStreamSource): List<Project> {
    val exceptions = mutableListOf<CsvParsingException.CsvRowErrors>()
    val projectIds = mutableListOf<String>()

    val projects = CSVReaderBuilder(
      InputStreamReader(projectCsv.inputStream, "UTF-8")
    ).withCSVParser(
      CSVParserBuilder().withSeparator(';').build()
    ).build().use { csvReader ->
      try {
        csvReader.readAll()
      } catch (e: CsvMalformedLineException) {
        throw CsvParsingException(
          listOf(
            CsvParsingException.CsvRowErrors(
              e.lineNumber.toInt(), listOf("malformed csv line")
            )
          )
        )
      }
    }.filterIndexed { i, _ ->
      // skip header row
      i > 0
    }.mapIndexed { rowNumber, rowValues ->
      if (rowValues.size != Column.values().size) {
        // This exception can not be combined
        // with other parsing issues. Add the
        // exception and jump to the next line.
        exceptions.add(
          CsvParsingException.CsvRowErrors(
            rowNumber, listOf("wrong column count")
          )
        )
        return@mapIndexed invalidProject()
      }

      val errorMessages = mutableListOf<String>()

      val row = RowParser(rowValues)

      val project = Project(
        id          = row.parse(Column.PROJECT_NUMBER),
        status      = row.parse(Column.PROJECT_STATUS),
        name        = row.parse(Column.PROJECT_NAME),
        provider = Provider(
          id        = row.parse(Column.PROVIDER_NUMBER),
          name      = row.parse(Column.PROJECT_PROVIDER)
        ),
        contactPerson = ContactPerson(
          pronoun   = row.parse(Column.PROJECT_PRONOUN),
          firstName = row.parse(Column.PROJECT_FIRSTNAME),
          lastName  = row.parse(Column.PROJECT_LASTNAME),
          email     = row.parse(Column.PROJECT_MAIL)
        ),
        goals       = row.parseGoals(),
        participants = Participants(
          age1to5   = row.parseInt(Column.PARTICIPANTS_AGE1TO5),
          age6to10  = row.parseInt(Column.PARTICIPANTS_AGE6TO10),
          age11to15 = row.parseInt(Column.PARTICIPANTS_AGE11TO15),
          age16to19 = row.parseInt(Column.PARTICIPANTS_AGE16TO19),
          age20to26 = row.parseInt(Column.PARTICIPANTS_AGE20TO26),
          worker    = row.parseInt(Column.PARTICIPANTS_WORKER)
        ),
        start       = row.parseDate(Column.PROJECT_START),
        end         = row.parseDate(Column.PROJECT_END)
      )

      errorMessages.addAll(row.errors)

      val violations = validator.validate(project)
      errorMessages.addAll(
        violations.map { it.message }
      )

      if (repository.existsById(project.id)) {
        // This exception can not be combined
        // with other parsing issues. Add the
        // exception and jump to the next line.
        exceptions.add(
          CsvParsingException.CsvRowErrors(
            rowNumber, listOf("project already exists")
          )
        )
        return@mapIndexed invalidProject()
      }



      if (projectIds.contains(project.id)) {
        errorMessages.add("duplicate project number")
      }

      if (errorMessages.isNotEmpty()) {
        exceptions.add(
          CsvParsingException.CsvRowErrors(
            rowNumber, errorMessages
          )
        )
      }

      projectIds.add(project.id)
      project
    }

    if (exceptions.isNotEmpty()) {
      throw CsvParsingException(exceptions)
    }

    return projects
  }
}

class CsvParsingException(val rows: List<CsvRowErrors>) :
  Exception("Error while parsing CSV data") {
  class CsvRowErrors(val csvRow: Int, val messages: List<String>)
}

private class RowParser(private val row: Array<String>) {

  val errors get() = _errors.toList()

  private val _errors = mutableListOf<String>()

  /**
   * Converts a String into an Int? If the String equals "NA" then
   * the [default] argument is used as return value
   */
  private fun parseInt(num: String, default: Int): Int =
    if (num == "NA") default else num.toInt()

  fun parse(column: Column): String = row[column.ordinal]

  fun parseInt(column: Column): Int = try {
    parseInt(row[column.ordinal], 0)
  } catch (e: NumberFormatException) {
    _errors.add("invalid number in '${column.csvName}': ${e.message}")
    0
  }

  fun parseDate(column: Column): LocalDateTime = try {
    parseDkjsDate(row[column.ordinal])
  } catch (e: DateTimeParseException) {
    _errors.add("invalid date in '${column.csvName}': ${e.message}")
    LocalDateTime.MIN
  }

  fun parseGoals(): Set<Int> = try {
    parse(Column.PROJECT_GOALS)
      .splitToSequence(',')
      .map { it.trim().toInt() }
      .toSet()
  } catch (e: NumberFormatException) {
    _errors.add("invalid goals specification in '${Column.PROJECT_GOALS.csvName}': ${e.message}")
    emptySet()
  }

}
