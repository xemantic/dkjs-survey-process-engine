/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 * Copyright (c) 2022 Abe Pazos / Xemantic
 */

package de.dkjs.survey

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.opencsv.exceptions.CsvMalformedLineException
import com.opencsv.processor.RowProcessor
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
    val exceptions = mutableListOf<CsvParsingException.RowResult>()
    val projectIds = mutableListOf<String>()
    val headerRowsToSkip = 1

    val csvParser = CSVParserBuilder()
      .withSeparator(';')
      .withIgnoreLeadingWhiteSpace(true)
      .build()

    val csvData = CSVReaderBuilder(
      InputStreamReader(projectCsv.inputStream, "UTF-8")
    ).withSkipLines(headerRowsToSkip)
      .withCSVParser(csvParser).build().use { csvReader ->
      try {
        csvReader.readAll()
      } catch (e: CsvMalformedLineException) {
        // Error 1: Malformed CSV. No Project nor CSV columns to show.
        // End of parsing.
        throw CsvParsingException("Malformed CSV on line ${e.lineNumber}, ${e.message}")
      }
    }.filter { rowValues -> rowValues.size > 1 || rowValues.first().isNotBlank() }

    if (csvData.isEmpty()) {
      // Error 2: File too short. No Project nor CSV columns to show.
      // End of parsing.
      throw CsvParsingException(
        "The CSV file should contain at least 2 " +
                "rows: a header row plus some data rows. Only 1 row found."
      )
    }

    val projects = csvData.mapIndexed { rowNumber, rowValues ->
      if (rowValues.size != Column.values().size) {
        // Error 3: Wrong column count. No Project, show CSV columns instead.
        val columns = rowValues.joinToString(", ", "[", "]")
        exceptions.add(
          CsvParsingException.RowResult(
            null, listOf("Wrong column count in line $rowNumber: $columns")
          )
        )
        // This exception can't be combined with other errors.
        // Exception added, jump to the next row.
        return@mapIndexed invalidProject()
      }

      val errorMessages = mutableListOf<String>()

      val row = RowParser(rowValues)

      val project = Project(
        id = row.parseString(Column.PROJECT_NUMBER),
        status = row.parseString(Column.PROJECT_STATUS),
        name = row.parseString(Column.PROJECT_NAME),
        provider = Provider(
          id = row.parseString(Column.PROVIDER_NUMBER),
          name = row.parseString(Column.PROJECT_PROVIDER)
        ),
        contactPerson = ContactPerson(
          pronoun = row.parseString(Column.PROJECT_PRONOUN),
          firstName = row.parseString(Column.PROJECT_FIRSTNAME),
          lastName = row.parseString(Column.PROJECT_LASTNAME),
          email = row.parseString(Column.PROJECT_MAIL)
        ),
        goals = row.parseGoals(),
        participants = Participants(
          age1to5 = row.parseInt(Column.PARTICIPANTS_AGE1TO5),
          age6to10 = row.parseInt(Column.PARTICIPANTS_AGE6TO10),
          age11to15 = row.parseInt(Column.PARTICIPANTS_AGE11TO15),
          age16to19 = row.parseInt(Column.PARTICIPANTS_AGE16TO19),
          age20to26 = row.parseInt(Column.PARTICIPANTS_AGE20TO26),
          worker = row.parseInt(Column.PARTICIPANTS_WORKER)
        ),
        start = row.parseDate(Column.PROJECT_START),
        end = row.parseDate(Column.PROJECT_END)
      )

      errorMessages.addAll(row.errors)

      val violations = validator.validate(project)
      errorMessages.addAll(
        violations.map { "Invalid value in '${it.propertyPath}': ${it.message}" }
      )

      if (repository.existsById(project.id)) {
        // Error 4: Project exists in DB. Show project.
        exceptions.add(
          CsvParsingException.RowResult(
            project, listOf("Project already exists in database")
          )
        )
        // This exception can't be combined with other errors.
        // Exception added, jump to the next row.
        return@mapIndexed invalidProject()
      }



      if (projectIds.contains(project.id)) {
        errorMessages.add("Two rows use the same project number: ${project
          .id} (Only the first one is shown above)")
      }

      if (errorMessages.isNotEmpty()) {
        // Error 5: Common parse error. Show details.
        exceptions.add(CsvParsingException.RowResult(project, errorMessages))
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


/**
 * An Exception that contains a List of rows, each row containing a list of
 * [String] error messages
 */
class CsvParsingException(val rows: List<RowResult>) :
  Exception(
    "Error while parsing CSV data: ${
      rows.flatMap { it.messages }.map { "\n$it" }
    }"
  ) {
  constructor(message: String) : this(listOf(RowResult(null, listOf(message))))

  class RowResult(var project: Project?, val messages: List<String>)
}

/**
 * Data container constructed from `Array<String>`.
 * Data can be queried by column index using various methods that convert
 * strings to other types like `Int`, `LocalDateTime`, or `Set<Int>`.
 * Conversion errors are collected in a list when calling such methods and
 * can be queried after parsing is completed.
 */
private class RowParser(private val row: Array<String>) {
  /**
   * Parse column as `String`
   */
  fun parseString(column: Column): String = row[column.ordinal]

  /**
   * Parse column as `Int`
   */
  fun parseInt(column: Column): Int? = try {
    val value = row[column.ordinal]
    if (value == "NA") null else value.toInt()
  } catch (e: NumberFormatException) {
    addError(column, e)
    null
  }

  /**
   * Parse column as `LocalDateTime`
   */
  fun parseDate(column: Column): LocalDateTime = try {
    parseDkjsDate(row[column.ordinal])
  } catch (e: DateTimeParseException) {
    addError(column, e)
    LocalDateTime.MIN
  }

  /**
   * Parse column as `Set<Int>` from a comma separated `String`
   */
  fun parseGoals(): Set<Int> = try {
    parseString(Column.PROJECT_GOALS)
      .splitToSequence(',')
      .map { it.trim().toInt() }
      .toSet()
  } catch (e: NumberFormatException) {
    addError(Column.PROJECT_GOALS, e)
    emptySet()
  }

  /**
   * Internal list where errors are appended to on failed parsing
   */
  private val _errors = mutableListOf<String>()

  /**
   * Append an error to the list
   */
  private fun addError(column: Column, e: Exception) =
    _errors.add("Invalid value in '${column.csvName}': " +
            "${e.javaClass.simpleName}: ${e.message}")

  /**
   * Public error list to query once all parsing is done
   */
  val errors get() = _errors.toList()
}
