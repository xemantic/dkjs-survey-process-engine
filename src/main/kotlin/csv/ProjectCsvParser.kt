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

/**
 * The result of parsing single row from the CSV file describing projects
 */
class RowResult(

  /**
   * The raw row output from the CSV parser.
   * Needed by the UI correlating parsing errors.
   */
  val csvRow: List<String>,

  /**
   * Successfully parsed project or `null` if CSV cannot be parsed.
   */
  val project: Project?,

  /**
   * The list of errors associated with this row.
   */
  val errors: List<Error>

) {

  interface Error {
    val message: String
  }

  data class RowError(
    override val message: String
  ) : Error

  data class ColumnError(
    override val message: String,
    val column: Column,
  ) : Error

}



/**
 * An Exception that contains a List of rows, each row containing a list of
 * [String] error messages
 */
class CsvParsingException(message: String? = null, val rows: List<RowResult> = emptyList()) : Exception(
  message ?: "Invalid data in rows: ${rows.reportRowsWithErrors()}"
)

/**
 * All the columns expected in the CSV file in their natural order.
 *
 * @param path The bean property path of this column after mapping
 *              to the [Project] model, needed for remapping JSR-303 validations
 *              back to CSV columns.
 */
enum class Column(
  val path: String,
  val type: Type = Type.TEXT
) {

  PROJECT_NUMBER("id"),
  PROJECT_STATUS("status"),
  PROJECT_PROVIDER("provider.name"),
  PROVIDER_NUMBER("provider.id"),
  PROJECT_PRONOUN("contactPerson.pronoun"),
  PROJECT_FIRSTNAME("contactPerson.firstName"),
  PROJECT_LASTNAME("contactPerson.lastName"),
  PROJECT_MAIL("contactPerson.email"),
  PROJECT_NAME("name"),
  PARTICIPANTS_AGE1TO5("participants.age1to5", Type.NUMERIC),
  PARTICIPANTS_AGE6TO10("participants.age6to10", Type.NUMERIC),
  PARTICIPANTS_AGE11TO15("participants.age11to15", Type.NUMERIC),
  PARTICIPANTS_AGE16TO19("participants.age16to19", Type.NUMERIC),
  PARTICIPANTS_AGE20TO26("participants.age20to26", Type.NUMERIC),
  PARTICIPANTS_WORKER("participants.worker", Type.NUMERIC),
  PROJECT_GOALS("goals"),
  PROJECT_START("start"),
  PROJECT_END("end");

  enum class Type {
    TEXT,
    NUMERIC
  }

  /**
   * The name of the column as it appears in CSV file header.
   */
  @Suppress("unused") // used in HTML template
  val csvName get() = name.replace('_', '.').lowercase()

  /**
   * Indicates if the column is numeric.
   */
  @Suppress("unused") // used in HTML template
  val isNumeric: Boolean get() = (type == Type.NUMERIC)

  companion object {

    private val pathToColumnMap: Map<String, Column> = values().associateBy { it.path }

    /**
     * Returns the [Column] instance based on given [Project] bean property path.
     *
     * @param path The bean property path of this column.
     * @throws IllegalArgumentException if the given `path` does not exist.
     */
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
   * Parses a CSV file creating a list of [Project] instances.
   *
   * @throws CsvParsingException in case of errors detected in input data.
   */
  fun parse(projectCsv: InputStreamSource): List<Project> {
    val batchContext = RowBatchContext()
    val results: List<RowResult> = projectCsv.toCsvReader().use { reader ->
      reader.sequenceRows().mapIndexed { rowIndex, rowResult ->
        val rowNumber = rowIndex + 1
        rowResult.fold(
          onFailure = { RowResult(
            emptyList(),
            project = null,
            listOf(RowResult.RowError(it.message!!)))
          },
          onSuccess = {
            val errors = mutableListOf<RowResult.Error>()
            val rowParser = RowParser(rowResult.getOrNull()!!)
            val project = rowParser.parseProject()
            errors.addAll(rowParser.errors)
            errors.addAll(validate(project))
            errors.addAll(batchContext.check(project, rowNumber))
            RowResult(it.toList(), project, errors.toList()) // ensure immutability
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

  private fun validate(project: Project): Collection<RowResult.ColumnError> =
    validator.validate(project).map {
      RowResult.ColumnError(
        it.message,
        Column.fromPath(it.propertyPath.toString())
      )
    }

  /**
   * Provides additional validity checks depending on the context of currently
   * processed CSV file. For example duplicates.
   */
  private inner class RowBatchContext {

    private val projectIdToRowMap: MutableMap<String, Int> = mutableMapOf()

    private val providerIdToRowAndProviderMap: MutableMap<String, Pair<Int, Provider>> = mutableMapOf()

    fun check(project: Project, rowNumber: Int): Collection<RowResult.ColumnError> {

      val errors = mutableListOf<RowResult.ColumnError>()

      val projectAlreadyDefined = projectIdToRowMap[project.id]
      if (projectAlreadyDefined == null) {
        projectIdToRowMap[project.id] = rowNumber
      } else {
        errors.add(
          RowResult.ColumnError(
            "already declared in row: $projectAlreadyDefined",
            Column.PROJECT_NUMBER
          )
        )
      }

      val providerAlreadyDefined = providerIdToRowAndProviderMap[project.provider.id]
      if (providerAlreadyDefined == null) {
        providerIdToRowAndProviderMap[project.provider.id] = Pair(rowNumber, project.provider)
      } else {
        if (project.provider.name != providerAlreadyDefined.second.name) {
          errors.add(
            RowResult.ColumnError(
              "already declared in row: "  +
                  "${providerAlreadyDefined.first} (under name " +
                  "\"${providerAlreadyDefined.second.name}\")",
              Column.PROVIDER_NUMBER
            )
          )
        }
        // this piece of code might be needed to assure that the same provider is inserted in the batch for all the projects
//        else {
//          project.provider = providerAlreadyDefined.second
//        }
      }

      providerRepository.findByIdOrNull(project.provider.id)?.let { existing ->
        if (project.provider.name != existing.name) {
          errors.add(
            RowResult.ColumnError(
              "provider with id '${project.provider.id}' " +
                  "already exists in the database under name: \"${existing.name}\"",
              Column.PROJECT_PROVIDER
            )
          )
        }
      }

      if (projectRepository.existsById(project.id)) {
        errors.add(
          RowResult.ColumnError(
            "project with id '${project.id}' already exists in the database",
            Column.PROJECT_NUMBER
          )
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
        _errors.add(
          RowResult.ColumnError(
            "is not a number, was: \"$it\"",
            column
          )
        )
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
      _errors.add(
        RowResult.ColumnError(
          "is not a valid date in format 'dd.mm.yyyy', was: \"$it\"",
          column
        )
      )
      LocalDateTime.MIN
    }
  }

  /**
   * Parse column as `Set<Int>` from a comma separated `String`
   */
  fun parseGoals(): List<Int> = parseString(Column.PROJECT_GOALS)
    .splitToSequence(',')
    .mapIndexed { index, value ->
      try {
        value.toInt()
      } catch (e: NumberFormatException) {
        _errors.add(
          RowResult.ColumnError(
            "must must consist of numbers in the range 1..7, was: \"$value\"",
            Column.PROJECT_GOALS
          )
        )
        // despite errors we are transforming goals to the sequence of
        // valid goal numbers, to avoid JSR-303 validation errors
        index + 1
      }
    }
    .toList()

  /**
   * Internal list where errors are appended to on failed parsing
   */
  private val _errors = mutableListOf<RowResult.ColumnError>()

  /**
   * Public error list to query once all parsing is done
   */
  val errors: List<RowResult.ColumnError> get() = _errors.toList()

}
