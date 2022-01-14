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
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@Component
class ProjectCsvParser @Inject constructor(
  private val repository: ProjectRepository
) {
  /**
   * Regex used to extract values out of "DD.MM.YYYY" String
   */
  private val rxDayMonthYear = """(\d\d)\.(\d\d)\.(\d\d\d\d)""".toRegex()

  /**
   * Converts a String into an Int? If the String equals "NA" then
   * the [default] argument is used as return value
   */
  private fun parseInt(num: String, default: Int): Int? =
    if (num == "NA") default else try {
      num.toInt()
    } catch (e: NumberFormatException) {
      null
    }

  /**
   * Converts a DD.MM.YYYY formatted String into a LocalDate object
   */
  private fun parseDate(dayMonthYear: String): LocalDate? =
    rxDayMonthYear.find(dayMonthYear)?.run {
      val (d, m, y) = this.destructured
      LocalDate.parse("$y-$m-$d")
    }


  /**
   * Create a [ProjectParsingResult] with an error message and a null [Project]
   */
  private fun projectParsingError(msg: String): ProjectParsingResult =
    ProjectParsingResult(null, "CSV parsing error: $msg")

  /**
   * Parses a CSV file creating a list of items, each item containing
   * a [Project] and null `message` on success, or
   * a null [Project] and a `message` describing why it couldn't be created.
   */
  fun parse(projectCsv: InputStreamSource): List<ProjectParsingResult> {
    val projectNumbers = mutableListOf<String>()

    val result = CSVReaderBuilder(
      InputStreamReader(projectCsv.inputStream, "ISO_8859-15")
    ).withCSVParser(
      CSVParserBuilder().withSeparator(';').build()
    ).build().use { csvReader ->
      try {
        csvReader.readAll()
      } catch (e: CsvMalformedLineException) {
        return listOf()
      }
    }.filterIndexed { i, _ ->
      i > 0 // skip header row
    }.map { row ->
      if (row.size != 18) {
        return@map projectParsingError("wrong column count (${row.size})")
      }

      var col = 0
      val pNumber = row[col++]
      val pStatus = row[col++]

      // Provider
      val pProvider = row[col++]
      val pProviderNumber = row[col++]

      // ContactPerson
      val pContactPronoun = row[col++]
      val pContactFirstname = row[col++]
      val pContactLastname = row[col++]
      val pContactMail = row[col++]

      // TODO: use proper e-mail validation
      if(pContactMail.count { it == '@'} != 1) {
        return@map projectParsingError("invalid e-mail")
      }

      val pName = row[col++]

      // Participants
      val pAge1to5 = parseInt(row[col++], 0)
        ?: return@map projectParsingError("invalid age1to5")
      val pAge6to10 = parseInt(row[col++], 0)
        ?: return@map projectParsingError("invalid age6to10")
      val pAge11to15 = parseInt(row[col++], 0)
        ?: return@map projectParsingError("invalid age11to15")
      val pAge16to19 = parseInt(row[col++], 0)
        ?: return@map projectParsingError("invalid age16to19")
      val pAge20to26 = parseInt(row[col++], 0)
        ?: return@map projectParsingError("invalid age20to26")
      val pWorker = parseInt(row[col++], 0)
        ?: return@map projectParsingError("invalid worker")

      val pGoals = row[col++].split(",").map { it.toInt() }.toSet()

      val pStart = parseDate(row[col++])
        ?: return@map projectParsingError("invalid start date")

      val pEnd = parseDate(row[col])
        ?: return@map projectParsingError("invalid end date")

      val provider = Provider(
        pProviderNumber,
        pProvider
      )
      val contactPerson = ContactPerson(
        pContactPronoun, pContactFirstname, pContactLastname, pContactMail
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

      if(projectNumbers.contains(pNumber)) {
        projectParsingError("duplicate project number")
      } else {
        projectNumbers.add(pNumber)
        ProjectParsingResult(project, null)
      }
    }
    return result
  }
}

// either project or a message when it cannot be parsed
class ProjectParsingResult(
  val project: Project?,
  val message: String?
)
