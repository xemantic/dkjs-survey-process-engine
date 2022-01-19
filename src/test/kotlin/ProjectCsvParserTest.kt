/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.ProjectRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.time.LocalDate

class ProjectCsvParserTest {
  private val repository = mockk<ProjectRepository>(relaxed = true)
  private val parser = ProjectCsvParser(repository)
  private fun csvToProjects(csv: String): List<Project> {
    val targetStream = ByteArrayInputStream(csv.toByteArray())
    return parser.parse { targetStream }
  }

  @Test
  fun `should parse project CSV file with single entry`() {
    // given
    val projectName = "expectedProjectName"
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"$projectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
    """.trimIndent()

    // when
    val projects = csvToProjects(csv)

    // then
    projects shouldHaveSize 1
    projects[0].name shouldBe projectName
  }

  @Test
  fun `should correctly parse dates from CSV`() {
    // given
    val startYear = "2021"
    val startMonth = "11"
    val startDay = "22"
    val endYear = "2022"
    val endMonth = "08"
    val endDay = "31"
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"projectName";0;0;250;50;0;NA;"01,05,03";"$startDay.$startMonth.$startYear";"$endDay.$endMonth.$endYear"
    """.trimIndent()

    // when
    val projects = csvToProjects(csv)

    // then
    projects shouldHaveSize 1
    projects[0].start shouldBe LocalDate.of(
      startYear.toInt(), startMonth.toInt(), startDay.toInt()
    )
    projects[0].end shouldBe LocalDate.of(
      endYear.toInt(), endMonth.toInt(), endDay.toInt()
    )
  }

  @Test
  fun `should not parse broken CSV`() {
    // given
    val csv = """
      "this file ends with a dangling double quote character";"x";"x"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"expectedProjectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022""
    """.trimIndent()

    // when
    val e = shouldThrow<CsvParsingException> {
      csvToProjects(csv)
    }
    // then
    e.rows.size shouldBe 1
    e.rows[0].messages shouldContain "malformed csv line"
  }

  @Test
  fun `should parse project CSV file with multiple entries`() {
    // given
    val projectName = List(5) { "project$it" }
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"${projectName[0]}";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
      "4022000131 -1";"50 - bewilligt";"very: important & club e.V.";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"${projectName[1]}";794;0;0;0;0;NA;"03,02,01";"17.01.2022";"01.07.2022"
      "4022000090 -1";"50 - bewilligt";"Über stringent society e.V.";345678;"Herr";"Max ";"Mustermann";"p3möglich@example.com";"${projectName[2]}";0;0;20;0;0;NA;"03,01,04";"09.03.2022";"09.03.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business  ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"${projectName[3]}";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4022000005 -1";"50 - bewilligt";"Fun e.V. - We have Ätzend fun!";456789;"Herr";"Mäxi";"Mäxi";"p5tetrapack@example.com";"${projectName[4]}";NA;NA;NA;NA;NA;23;"01,07";"09.01.2022";"31.03.2022"
    """.trimIndent()

    // when
    val projects = csvToProjects(csv)

    // then
    projects shouldHaveSize 5
    projectName shouldBe projects.map { it.name }
  }

  @Test
  fun `should report error if project is specified twice`() {
    // given
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4022000131 -1";"50 - bewilligt";"very: important & club e.V.";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"Eva4everYeah";794;0;0;0;0;NA;"03,02,01";"17.01.2022";"01.07.2022"
      "4022000090 -1";"50 - bewilligt";"Über stringent society e.V.";345678;"Herr";"Max ";"Mustermann";"p3möglich@example.com";"Max; Out IT with Ã¤Ã¼Ã¶&20~";0;0;20;0;0;NA;"03,01,04";"09.03.2022";"09.03.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business  ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"Give ducks more rights";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business  ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"Give ducks more rights";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4022000005 -1";"50 - bewilligt";"Fun e.V. - We have Ätzend fun!";456789;"Herr";"Mäxi";"Mäxi";"p5tetrapack@example.com";"Amusement Park for ducks";NA;NA;NA;NA;NA;23;"01,07";"09.01.2022";"31.03.2022"
    """.trimIndent()

    // when
    val e = shouldThrow<CsvParsingException> {
      csvToProjects(csv)
    }

    // then
    e.rows.any { it.messages.contains("duplicate project number") } shouldBe true
  }

  @Test
  fun `should report error if CSV file contains incorrect data`() {
    // given
    val invalidEmail = "p1urtümlich@example@.com"
    val invalidStart = "17-01-2022"
    val invalidEnd = "01-07-2022"
    val nonValidWorker = "FOO"
    val age1to5 = "A"
    val csv = """
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"invalid e-mail";123456;"Frau";"Maxi";"Musterfräulein";"$invalidEmail";"project1";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
      "4022000131 -1";"50 - bewilligt";"invalid project.start";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"project2";794;0;0;0;0;NA;"03,02,01";"$invalidStart";"01.07.2022"
      "4022000132 -1";"50 - bewilligt";"invalid project.end";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"project2";794;0;0;0;0;NA;"03,02,01";"17.01.2022";"$invalidEnd"
      "4022000090 -1";"50 - bewilligt";"invalid participants.worker";345678;"Herr";"Max ";"Mustermann";"p3möglich@example.com";"project3";0;0;20;0;0;$nonValidWorker;"03,01,04";"09.03.2022";"09.03.2022"
      "4021000014 -2";"50 - bewilligt";"invalid participants.ageXtoY";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"project4";$age1to5;B;C;D;E;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4022000005 -1";"50 - bewilligt";"invalid column count";456789;"Herr";"Mäxi";"Mäxi";"p5tetrapack@example.com";"project5"
    """.trimIndent()

    // when
    val e = shouldThrow<CsvParsingException> {
      csvToProjects(csv)
    }

    // then
    e.rows[0].messages shouldContain "invalid e-mail"
    e.rows[1].messages shouldContain "invalid start date"
    e.rows[2].messages shouldContain "invalid end date"
    e.rows[3].messages shouldContain "invalid worker"
    e.rows[4].messages shouldContainAll listOf(
      "invalid age1to5",
      "invalid age6to10",
      "invalid age11to15",
      "invalid age16to19",
      "invalid age20to26"
    )
    e.rows[5].messages shouldContain "wrong column count"
  }

  @Test
  fun `should report error if project already exists`() {
    // given
    val id = "4021000014 -1"
    val csv = """
      "x";"x";"x"
      "$id";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"expectedProjectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
    """.trimIndent()
    every { repository.existsById(id) } answers { true }

    // when
    val e = shouldThrow<CsvParsingException> {
      csvToProjects(csv)
    }

    // then
    e.rows[0].messages shouldContain "project already exists"
  }
}
