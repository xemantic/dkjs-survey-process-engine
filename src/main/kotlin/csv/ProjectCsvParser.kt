/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 * Copyright (c) 2022 Abe Pazos / Xemantic
 */

package de.dkjs.survey.csv

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.exceptions.CsvMalformedLineException
import de.dkjs.survey.model.*
import de.dkjs.survey.time.parseDkjsDate
import org.springframework.core.io.InputStreamSource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

class RowResult(
  val csvRow: Array<String>,
  val project: Project?,
  val errors: List<String>
)

/**
 * An Exception that contains a List of rows, each row containing a list of
 * [String] error messages
 */
class CsvParsingException(message: String? = null, val rows: List<RowResult> = emptyList()) : Exception(
  message ?: "Invalid data in rows: ${rows.reportRowsWithErrors()}"
)

private enum class Column(val path: String) {

  PROJECT_NUMBER("id"),
  PROJECT_STATUS("status"),
  PROJECT_PROVIDER("provider.name"),
  PROVIDER_NUMBER("provider.id"),
  PROJECT_PRONOUN("contactPerson.pronoun"),
  PROJECT_FIRSTNAME("contactPerson.firstName"),
  PROJECT_LASTNAME("contactPerson.lastName"),
  PROJECT_MAIL("contactPerson.email"),
  PROJECT_NAME("name"),
  PARTICIPANTS_AGE1TO5("participants.age1to5"),
  PARTICIPANTS_AGE6TO10("participants.age6to10"),
  PARTICIPANTS_AGE11TO15("participants.age11to15"),
  PARTICIPANTS_AGE16TO19("participants.age16to19"),
  PARTICIPANTS_AGE20TO26("participants.age20to26"),
  PARTICIPANTS_WORKER("participants.worker"),
  PROJECT_GOALS("goals"),
  PROJECT_START("start"),
  PROJECT_END("end");

  val csvName get() = name.replace('_', '.').lowercase()

  companion object {

    private val pathToColumnMap: Map<String, Column> = values()
      .map { Pair(it.path, it) }
      .toMap()

    fun fromPath(path: String): Column =
      pathToColumnMap[path]
        ?: throw IllegalArgumentException(
          "Invalid model path of CSV column, path: $path"
        )

  }

}

@Singleton
@Component
class ProjectCsvParser @Inject constructor(
  private val projectRepository: ProjectRepository,
  private val providerRepository: ProviderRepository,
  private val validator: Validator
) {

  /**
   * Parses a CSV file creating a list of items, each item containing
   * a [Project] and null `message` on success, or
   * a null [Project] and a `message` describing why it couldn't be created.
   */
  fun parse(projectCsv: InputStreamSource): List<Project> {
    val batchContext = RowBatchContext()
    val results: List<RowResult> = projectCsv.toCsvReader().use { reader ->
      reader.sequenceRows().mapIndexed { rowIndex, rowResult ->
        val rowNumber = rowIndex + 1
        rowResult.fold(
          onFailure = { RowResult(emptyArray(), project = null, listOf(it.message!!)) },
          onSuccess = {
            val errors = mutableListOf<String>()
            val rowParser = RowParser(rowResult.getOrNull()!!)
            val project = rowParser.parseProject()
            errors.addAll(rowParser.errors)
            errors.addAll(validate(project))
            errors.addAll(batchContext.check(project, rowNumber))
            RowResult(it, project, errors.toList())
          }
        )
      }.toList()
    }

    if (results.isEmpty()) {
      throw CsvParsingException(
        "The CSV file should contain at least one header row and one data row"
      )
    }

    if (results.any { it.errors.isNotEmpty() }) {
      throw CsvParsingException(rows = results)
    }

    return results.map { it.project!! }
  }

  private fun validate(project: Project): Collection<String> =
    validator.validate(project).map {
      val column = Column.fromPath(it.propertyPath.toString()).csvName
      "'$column': ${it.message}"
    }

  private inner class RowBatchContext {

    private val projectIdToRowMap: MutableMap<String, Int> = mutableMapOf()

    private val providerIdToRowAndNameMap: MutableMap<String, Pair<Int, Provider>> = mutableMapOf()

    fun check(project: Project, rowNumber: Int): Collection<String> {

      val errors = mutableListOf<String>()

      val projectAlreadyDefined = projectIdToRowMap[project.id]
      if (projectAlreadyDefined == null) {
        projectIdToRowMap[project.id] = rowNumber
      } else {
        errors.add("'${Column.PROJECT_NUMBER.csvName}': already declared in row: $projectAlreadyDefined")
      }

      val providerAlreadyDefined = providerIdToRowAndNameMap[project.provider.id]
      if (providerAlreadyDefined == null) {
        providerIdToRowAndNameMap[project.provider.id] = Pair(rowNumber, project.provider)
      } else {
        if (project.provider.name != providerAlreadyDefined.second.name) {
          errors.add("'${Column.PROVIDER_NUMBER.csvName}': already declared in row: " +
              "${providerAlreadyDefined.first} (under name " +
              "\"${providerAlreadyDefined.second.name}\")")
        }
        // this piece of code might be needed to assure that the same provider is inserted in the batch for all the projects
//        else {
//          project.provider = providerAlreadyDefined.second
//        }
      }

      providerRepository.findByIdOrNull(project.provider.id)?.let { existing ->
        if (project.provider.name != existing.name) {
          errors.add(
            "'${Column.PROJECT_PROVIDER.csvName}': provider with id '${project.provider.id}' " +
                "already exists in the database under name: \"${existing.name}\""
          )
        }
      }

      if (projectRepository.existsById(project.id)) {
        errors.add(
          "'${Column.PROJECT_NUMBER.csvName}': project with id " +
              "'${project.id}' already exists in the database"
        )
      }

      return errors.toList()
    }
  }

}

private fun RowParser.parseProject() = Project(
  id = parseString(Column.PROJECT_NUMBER),
  status = parseString(Column.PROJECT_STATUS),
  name = parseString(Column.PROJECT_NAME),
  provider = Provider(
    id = parseString(Column.PROVIDER_NUMBER),
    name = parseString(Column.PROJECT_PROVIDER)
  ),
  contactPerson = ContactPerson(
    pronoun = parseString(Column.PROJECT_PRONOUN),
    firstName = parseString(Column.PROJECT_FIRSTNAME),
    lastName = parseString(Column.PROJECT_LASTNAME),
    email = parseString(Column.PROJECT_MAIL)
  ),
  goals = parseGoals(),
  participants = Participants(
    age1to5 = parseInt(Column.PARTICIPANTS_AGE1TO5),
    age6to10 = parseInt(Column.PARTICIPANTS_AGE6TO10),
    age11to15 = parseInt(Column.PARTICIPANTS_AGE11TO15),
    age16to19 = parseInt(Column.PARTICIPANTS_AGE16TO19),
    age20to26 = parseInt(Column.PARTICIPANTS_AGE20TO26),
    worker = parseInt(Column.PARTICIPANTS_WORKER)
  ),
  start = parseDate(Column.PROJECT_START),
  end = parseDate(Column.PROJECT_END)
)

private fun InputStreamSource.toCsvReader(): CSVReader =
  CSVReaderBuilder(InputStreamReader(this.inputStream, "UTF-8"))
    .withSkipLines(1)
    .withCSVParser(newCsvParser())
    .build()

private fun newCsvParser() = CSVParserBuilder()
  .withSeparator(';')
  .withIgnoreLeadingWhiteSpace(true)
  .build()

private fun CSVReader.sequenceRows(): Sequence<Result<Array<String>>> =
  sequence {
    while (true) {
      yield(
        try {
          val row = readNext() ?: break
          if (row.size == Column.values().size) Result.success(row)
          else Result.failure(
            CsvParsingException(
              "Wrong CSV column count, expected ${Column.values().size}, but was ${row.size}"
            )
          )
        } catch (e: CsvMalformedLineException) {
          Result.failure(e)
        }
      )
    }
  }

private fun List<RowResult>.reportRowsWithErrors(): String =
  this.mapIndexedNotNull { index, result ->
    if (result.errors.isNotEmpty()) index + 1 else null
  }.joinToString(", ")

/**
 * Data container constructed from `Array<String>`.
 * Data can be queried by column index using various methods that convert
 * strings to other types like [Int], [LocalDateTime], or [Set]`<Int>`.
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
  fun parseInt(column: Column): Int? = parseString(column).let {
    if (it == "NA") null
    else {
      try {
        it.toInt()
      } catch (e: NumberFormatException) {
        _errors.add("'${column.csvName}': is not a number, was: \"$it\"")
        null
      }
    }
  }

  /**
   * Parse column as `LocalDateTime`
   */
  fun parseDate(column: Column): LocalDateTime = parseString(column).let {
    try {
      parseDkjsDate(it)
    } catch (e: DateTimeParseException) {
      _errors.add("'${column.csvName}': is not a valid date in format 'dd.mm.yyyy', was: \"$it\"")
      LocalDateTime.MIN
    }
  }

  /**
   * Parse column as `Set<Int>` from a comma separated `String`
   */
  fun parseGoals(): Set<Int> = parseString(Column.PROJECT_GOALS)
    .splitToSequence(',')
    .mapIndexed { index, value ->
      try {
        value.toInt()
      } catch (e: NumberFormatException) {
        _errors.add(
          "'${Column.PROJECT_GOALS.csvName}': must must consist of numbers " +
              "in the range 1..7, was: \"$value\""
        )
        // despite errors we are transforming goals to the sequence of
        // valid goal numbers, to avoid JSR-303 validation errors
        index + 1
      }
    }
    .toSet()

  /**
   * Internal list where errors are appended to on failed parsing
   */
  private val _errors = mutableListOf<String>()

  /**
   * Public error list to query once all parsing is done
   */
  val errors get() = _errors.toList()

}
