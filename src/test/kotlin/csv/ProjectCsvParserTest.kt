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

package de.dkjs.survey.csv

import de.dkjs.survey.csv.Column.*
import de.dkjs.survey.csv.RowResult.RowError
import de.dkjs.survey.model.ProjectRepository
import de.dkjs.survey.model.Provider
import de.dkjs.survey.model.ProviderRepository
import de.dkjs.survey.test.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import javax.validation.Validation

class ProjectCsvParserTest {

  @Test
  fun `should parse project CSV file with single entry`() {
    // given
    databaseIsEmpty()
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Project Foo";1;6;11;16;20;3;"01,05,03";"22.11.2021";"31.08.2022"
    """

    // when
    val projects = parser.parse(csv)

    // then
    projects shouldHaveSize 1
    with (projects[0]) {
      id shouldBe "4021000014 -1"
      name shouldBe "Project Foo"
      status shouldBe "50 - bewilligt"
      goals shouldBe setOf(1, 5, 3)
      with (contactPerson) {
        pronoun shouldBe "Frau"
        firstName shouldBe "Maxi"
        lastName shouldBe "Musterfräulein"
        email shouldBe "p1urtümlich@example.com"
      }
      with (participants) {
        age1to5 shouldBe 1
        age6to10 shouldBe 6
        age11to15 shouldBe 11
        age16to19 shouldBe 16
        age20to26 shouldBe 20
        worker shouldBe 3
      }
      with (provider) {
        id shouldBe "123456"
        name shouldBe "serious; business ÖA GmbH"
      }
      start shouldBe startOfDay(2021, 11, 22)
      end shouldBe startOfDay(2022, 8, 31)
      surveyProcess shouldBe null
    }
  }

  @Test
  fun `should parse project CSV file with multiple entries`() {
    // given
    databaseIsEmpty()
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"FooA";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
      "4022000131 -1";"50 - bewilligt";"very: important & club e.V.";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"FooB";794;0;0;0;0;NA;"03,02,01";"17.01.2022";"01.07.2022"
      "4022000090 -1";"50 - bewilligt";"Über stringent society e.V.";345678;"Herr";"Max ";"Mustermann";"p3möglich@example.com";"FooC";0;0;20;0;0;NA;"03,01,04";"09.03.2022";"09.03.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"FooD";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4022000005 -1";"50 - bewilligt";"Fun e.V. - We have Ätzend fun!";456789;"Herr";"Mäxi";"Mäxi";"p5tetrapack@example.com";"FooE";NA;NA;NA;NA;NA;23;"01,07";"09.01.2022";"31.03.2022"
    """

    // when
    val projects = parser.parse(csv)

    // then
    projects shouldHaveSize 5
    projects[0].name shouldBe "FooA"
    projects[1].name shouldBe "FooB"
    projects[2].name shouldBe "FooC"
    projects[3].name shouldBe "FooD"
    projects[4].name shouldBe "FooE"
  }

  @Test
  fun `should correctly parse 'NA' string (not available) in participant age groups`() {
    // given
    databaseIsEmpty()
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Project Foo";NA;NA;NA;NA;NA;NA;"01,05,03";"22.11.2021";"31.08.2022"
    """

    // when
    val projects = parser.parse(csv)

    // then
    projects shouldHaveSize 1
    with (projects[0].participants) {
      age1to5 shouldBe null
      age6to10 shouldBe null
      age11to15 shouldBe null
      age16to19 shouldBe null
      age20to26 shouldBe null
      worker shouldBe null
    }
  }

  @Test
  fun `should not parse empty file`() {
    // given
    databaseIsEmpty()
    val csv = ""

    val errors = shouldThrow<CsvParsingException> {
      // when
      parser.parse(csv)
    }

    // then
    errors shouldHaveMessage "The CSV file should contain at least one header row and one data row"
    errors.rows shouldHaveSize 0
  }

  @Test
  fun `should not parse file containing only header`() {
    // given
    databaseIsEmpty()
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
    """

    val errors = shouldThrow<CsvParsingException> {
      // when
      parser.parse(csv)
    }

    // then
    errors shouldHaveMessage "The CSV file should contain at least one header row and one data row"
    errors.rows shouldHaveSize 0
  }

  @Test
  fun `should not parse broken CSV`() {
    // given
    databaseIsEmpty()
    val csv = """
      "this file contains multiple CSV formatting errors";"x";"x"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"expectedProjectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021"
      "4021000014 -2";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Project Foo";1;6;11;16;20;3;"01,05,03";"22.11.2021";"31.08.2022"
      "4021000014 -3";"50 - bewilligt";"invalid column count";456789;"Herr";"Mäxi";"Mäxi";"p5tetrapack@example.com";"project5"            
      "4021000014 -4";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"expectedProjectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.09.2022""
    """

    val errors = shouldThrow<CsvParsingException> {
      // when
      parser.parse(csv)
    }

    // then
    // the last line is corrupted resulting in extra rows added by confused parser
    errors shouldHaveMessage "Invalid data in rows: 1, 3, 4, 5, 6"
    errors.rows shouldHaveSize 6
    errors.shouldReportRow(1, RowError("Wrong CSV column count, expected 18, but was 17"))
    errors.shouldNotReportRow(2)
    errors.shouldReportRow(3, RowError("Wrong CSV column count, expected 18, but was 9"))
    errors.shouldReportRow(4, RowError("Unterminated quoted field at end of CSV line. Beginning of lost text: [31.09.2022\"\n]"))
    errors.shouldReportRow(5, RowError("Wrong CSV column count, expected 18, but was 17"))
    errors.shouldReportRow(6, RowError("Unterminated quoted field at end of CSV line. Beginning of lost text: [31.09.2022\"\n]"))
  }

  @Test
  fun `should report errors if CSV file contains invalid data`() {
    // given
    databaseIsEmpty()
    val invalidEmail = "p1urtümlich@example@.com"
    val invalidStart = "17-01-2022"
    val invalidEnd = "01-07-2022"
    val invalidParticipantCount = "FOO"
    val invalidWorkerCount = "BAR"
    val invalidGoalsNotNumbers = "01,foo,bar"
    val invalidGoalsNotIn1To7Range = "01,-02,42"
    val invalidGoalsTooMany = "01,02,03,04"
    val invalidGoals01Missing = "02,03"
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "";"";"";;"";"";"";"";"";-1;0;1000000;0;$invalidParticipantCount;$invalidWorkerCount;"$invalidGoalsNotNumbers";"";""
      "4022000132 -1";"50 - bewilligt";"foo";234567;"Frau";"Maxi";"Musterfrau";"$invalidEmail";"project2";794;0;0;0;0;NA;"$invalidGoalsNotIn1To7Range";"$invalidStart";"$invalidEnd"
      "4022000131 -1";"50 - bewilligt";"very: important & club e.V.";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"FooB";794;0;0;0;0;NA;"$invalidGoalsTooMany";"17.01.2022";"01.07.2022"
      "4022000131 -2";"50 - bewilligt";"very: important & club e.V.";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"FooB";794;0;0;0;0;NA;"$invalidGoals01Missing";"17.01.2022";"01.07.2022"
    """

    val errors = shouldThrow<CsvParsingException> {
      // when
      parser.parse(csv)
    }

    // then
    errors shouldHaveMessage "Invalid data in rows: 1, 2, 3, 4"
    errors.rows shouldHaveSize 4
    errors.shouldReportRow(1,
      PROJECT_NUMBER that "must not be empty",
      PROJECT_NAME that "must not be empty",
      PROJECT_STATUS that "must not be empty",
      PROJECT_PROVIDER that "must not be empty",
      PROVIDER_NUMBER that "must not be empty",
      PROJECT_PRONOUN that "must not be empty",
      PROJECT_FIRSTNAME that "must not be empty",
      PROJECT_LASTNAME that "must not be empty",
      PROJECT_MAIL that "must not be empty",
      PARTICIPANTS_AGE1TO5 that "must be greater than or equal to 0",
      PARTICIPANTS_AGE20TO26 that "is not a number, was: \"FOO\"",
      PARTICIPANTS_WORKER that "is not a number, was: \"BAR\"",
      PROJECT_GOALS that "must must consist of numbers in the range 1..7, was: \"foo\"",
      PROJECT_GOALS that "must must consist of numbers in the range 1..7, was: \"bar\"",
      PROJECT_START that "is not a valid date in format 'dd.mm.yyyy', was: \"\"",
      PROJECT_END that "is not a valid date in format 'dd.mm.yyyy', was: \"\""
    )
    errors.shouldReportRow(2,
      PROJECT_MAIL that "must be a well-formed email address",
      PROJECT_GOALS that "must be a set of integer numbers within 1..7 range and 1 must be always present",
      PROJECT_START that "is not a valid date in format 'dd.mm.yyyy', was: \"$invalidStart\"",
      PROJECT_END that "is not a valid date in format 'dd.mm.yyyy', was: \"$invalidEnd\""
    )
    errors.shouldReportRow(3,
      PROJECT_GOALS that "size must be between 1 and 3"
    )
    errors.shouldReportRow(4,
      PROJECT_GOALS that "must be a set of integer numbers within 1..7 range and 1 must be always present"
    )
  }

  @Test
  fun `should report errors if project with the same number is specified multiple times`() {
    // given
    databaseIsEmpty()
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -2";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"Give ducks more rights1";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"expectedProjectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"Give ducks more rights2";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4022000090 -1";"50 - bewilligt";"Über stringent society e.V.";345678;"Herr";"Max ";"Mustermann";"p3möglich@example.com";"FooC";0;0;20;0;0;NA;"03,01,04";"09.03.2022";"09.03.2022"
      "4022000090 -1";"50 - bewilligt";"Über stringent society e.V.";345678;"Herr";"Max ";"Mustermann";"p3möglich@example.com";"FooC";0;0;20;0;0;NA;"03,01,04";"09.03.2022";"09.03.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business  ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"Give ducks more rights1";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
    """

    val errors = shouldThrow<CsvParsingException> {
      // when
      parser.parse(csv)
    }

    //then
    errors shouldHaveMessage "Invalid data in rows: 3, 5, 6"
    errors.rows shouldHaveSize 6
    errors.shouldReportRow(3, PROJECT_NUMBER that "already declared in row: 1")
    errors.shouldReportRow(5, PROJECT_NUMBER that "already declared in row: 4")
    errors.shouldReportRow(6, PROJECT_NUMBER that "already declared in row: 1")
  }

  @Test
  fun `should allow repeated provider which does not exist in the database yet`() {
    // given
    databaseIsEmpty()
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"FooA";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"FooB";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
    """

    // when
    val projects = parser.parse(csv)

    // then
    projects shouldHaveSize 2
    projects[0].id shouldBe "4021000014 -1"
    projects[1].id shouldBe "4021000014 -2"
  }

  @Test
  fun `should report error if provider, which does not exist in the database, is declared twice with different name`() {
    // given
    databaseIsEmpty()
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"FooA";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
      "4021000014 -2";"50 - bewilligt";"serious typo; business ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"FooB";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
    """

    val errors = shouldThrow<CsvParsingException> {
      // when
      parser.parse(csv)
    }

    //then
    errors shouldHaveMessage "Invalid data in rows: 2"
    errors.rows shouldHaveSize 2
    errors.shouldReportRow(2, PROVIDER_NUMBER that "already declared in row: 1 (under name \"serious; business ÖA GmbH\")")
  }

  @Test
  fun `should allow provider which already exists in the database under the same name`() {
    // given
    val projectId = "4021000014 -1"
    val provider = Provider(
      "123456",
      "serious; business ÖA GmbH"
    )
    every { providerRepository.findByIdOrNull(provider.id) } returns provider
    every { projectRepository.existsById(projectId) } returns false
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"${provider.name}";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"FooA";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
    """

    // when
    val projects = parser.parse(csv)

    // then
    projects shouldHaveSize 1
    projects[0].id shouldBe projectId
  }

  @Test
  fun `should report error if provider already exists in the database under different name`() {
    // given
    val projectId = "4021000014 -1"
    val provider = Provider(
      "123456",
      "serious; business ÖA GmbH"
    )
    val differentProviderName = "serious typo; business ÖA GmbH"
    every { providerRepository.findByIdOrNull(provider.id) } returns provider
    every { projectRepository.existsById(projectId) } returns false
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"$differentProviderName";${provider.id};"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"FooB";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
    """

    val errors = shouldThrow<CsvParsingException> {
      // when
      parser.parse(csv)
    }

    //then
    errors shouldHaveMessage "Invalid data in rows: 1"
    errors.rows shouldHaveSize 1
    errors.shouldReportRow(1, PROJECT_PROVIDER that "provider with id '123456' already exists in the database under name: \"serious; business ÖA GmbH\"")
  }

  @Test
  fun `should report error if project already exists in the database`() {
    // given
    val id = "4021000014 -1"
    val provider = Provider(
      "123456",
      "serious; business ÖA GmbH"
    )
    every { providerRepository.findByIdOrNull(provider.id) } returns provider
    every { projectRepository.existsById(id) } returns true
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$id";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"expectedProjectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
    """

    // when
    val errors = shouldThrow<CsvParsingException> {
      parser.parse(csv)
    }

    // then
    errors shouldHaveMessage "Invalid data in rows: 1"
    errors.rows shouldHaveSize 1
    errors.shouldReportRow(1, PROJECT_NUMBER that "project with id '4021000014 -1' already exists in the database")
  }

  // -- test utilities

  private fun databaseIsEmpty() {
    every { providerRepository.findById(any()) } returns Optional.empty()
    every { projectRepository.existsById(any()) } returns false
  }

  private val validator = Validation.buildDefaultValidatorFactory().validator

  private val providerRepository = mockk<ProviderRepository>()

  private val projectRepository = mockk<ProjectRepository>()

  private val parser = ProjectCsvParser(
    projectRepository,
    providerRepository,
    validator
  )

}
