/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 * Copyright (c) 2022 Abe Pazos / Xemantic
 */

package de.dkjs.survey

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.opencsv.exceptions.CsvMalformedLineException
import de.dkjs.survey.model.*
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Component
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@Component
class ProjectCsvParser @Inject constructor(
  private val repository: ProjectRepository
) {
  private val csvDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

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
    LocalDate.MIN,
    LocalDate.MIN,
    null
  )

  /**
   * Converts a String into an Int? If the String equals "NA" then
   * the [default] argument is used as return value
   */
  private fun parseInt(num: String, default: Int): Int =
    if (num == "NA") default else num.toInt()

  /**
   * Converts a DD.MM.YYYY formatted String into a LocalDate object
   */
  private fun parseDate(dayMonthYear: String) =
    LocalDate.from(csvDateFormat.parse(dayMonthYear))

  /**
   * Parses a CSV file creating a list of items, each item containing
   * a [Project] and null `message` on success, or
   * a null [Project] and a `message` describing why it couldn't be created.
   */
  fun parse(projectCsv: InputStreamSource): List<Project> {
    val exceptions = mutableListOf<CsvParsingException.CsvRowErrors>()
    val projectNumbers = mutableListOf<String>()

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
    }.mapIndexed { rowNumber, row ->
      if (row.size != 18) {
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

      var col = 0
      val pNumber = row[col++]
      val pStatus = row[col++]

      if (repository.existsById(pNumber)) {
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

      val errorMessages = mutableListOf<String>()

      // Provider
      val pProvider = row[col++]
      val pProviderNumber = row[col++]

      // ContactPerson
      val pContactPronoun = row[col++]
      val pContactFirstname = row[col++]
      val pContactLastname = row[col++]
      val pContactMail = row[col++]

      // TODO: use proper e-mail validation
      if (pContactMail.count { it == '@' } != 1) {
        errorMessages.add("invalid e-mail")
      }

      val pName = row[col++]

      // Participants
      val pAge1to5 = try {
        parseInt(row[col++], 0)
      } catch (e: NumberFormatException) {
        errorMessages.add("invalid age1to5")
        0
      }
      val pAge6to10 = try {
        parseInt(row[col++], 0)
      } catch (e: NumberFormatException) {
        errorMessages.add("invalid age6to10")
        0
      }
      val pAge11to15 = try {
        parseInt(row[col++], 0)
      } catch (e: NumberFormatException) {
        errorMessages.add("invalid age11to15")
        0
      }
      val pAge16to19 = try {
        parseInt(row[col++], 0)
      } catch (e: NumberFormatException) {
        errorMessages.add("invalid age16to19")
        0
      }
      val pAge20to26 = try {
        parseInt(row[col++], 0)
      } catch (e: NumberFormatException) {
        errorMessages.add("invalid age20to26")
        0
      }
      val pWorker = try {
        parseInt(row[col++], 0)
      } catch (e: NumberFormatException) {
        errorMessages.add("invalid worker")
        0
      }

      val pGoals = row[col++].split(",")
        .map { it.toInt() }.toSet()

      val pStart = try {
        parseDate(row[col++])
      } catch (e: DateTimeParseException) {
        errorMessages.add("invalid start date")
        LocalDate.MIN
      }

      val pEnd = try {
        parseDate(row[col])
      } catch (e: DateTimeParseException) {
        errorMessages.add("invalid end date")
        LocalDate.MIN
      }

      val provider = Provider(
        pProviderNumber,
        pProvider
      )
      val contactPerson = ContactPerson(
        pContactPronoun,
        pContactFirstname,
        pContactLastname,
        pContactMail
      )
      val participants = Participants(
        pAge1to5,
        pAge6to10,
        pAge11to15,
        pAge16to19,
        pAge20to26,
        pWorker
      )

      val project = Project(
        id = pNumber,
        status = pStatus,
        name = pName,
        provider = provider,
        contactPerson = contactPerson,
        goals = pGoals,
        participants = participants,
        start = pStart,
        end = pEnd,
        surveyProcess = null
      )

      if (projectNumbers.contains(pNumber)) {
        errorMessages.add("duplicate project number")
      }

      if (errorMessages.isNotEmpty()) {
        exceptions.add(
          CsvParsingException.CsvRowErrors(
            rowNumber, errorMessages
          )
        )
      }

      projectNumbers.add(pNumber)
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
