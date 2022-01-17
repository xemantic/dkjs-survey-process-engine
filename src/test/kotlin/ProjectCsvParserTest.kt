/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.model.ProjectRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class ProjectCsvParserTest {
  private val repository = mockk<ProjectRepository>(relaxed = true)
  private val parser = ProjectCsvParser(repository)
  private fun csvToProjects(csv: String): List<ProjectParsingResult> {
    val targetStream = ByteArrayInputStream(csv.toByteArray())
    return parser.parse { targetStream }
  }

  @Test
  fun `should parse project CSV file with single entry`() {
    // given
    val projectName = "expectedProjectName"

    // when
    val results = csvToProjects("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"$projectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"      
    """.trimIndent())

    // then
    results shouldHaveSize 1
    val result = results[0]
    result.message shouldBe null
    result.project shouldNotBe null
    val project = result.project!!
    project.name shouldBe projectName
  }

  @Test
  fun `should not parse broken CSV`() {
    // given

    // when
    val results = csvToProjects("""
      "this file ends with a dangling double quote character";"x";"x"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"expectedProjectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022""    
    """.trimIndent())

    // then
    results.shouldBeEmpty()
  }

  @Test
  fun `should parse project CSV file with multiple entries`() {
    // given
    val projectName = List(5) { "project$it"}

    // when
    val results = csvToProjects("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"${projectName[0]}";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
      "4022000131 -1";"50 - bewilligt";"very: important & club e.V.";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"${projectName[1]}";794;0;0;0;0;NA;"03,02,01";"17.01.2022";"01.07.2022"
      "4022000090 -1";"50 - bewilligt";"Über stringent society e.V.";345678;"Herr";"Max ";"Mustermann";"p3möglich@example.com";"${projectName[2]}";0;0;20;0;0;NA;"03,01,04";"09.03.2022";"09.03.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business  ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"${projectName[3]}";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4022000005 -1";"50 - bewilligt";"Fun e.V. - We have Ätzend fun!";456789;"Herr";"Mäxi";"Mäxi";"p5tetrapack@example.com";"${projectName[4]}";NA;NA;NA;NA;NA;23;"01,07";"09.01.2022";"31.03.2022"      
    """.trimIndent())

    // then
    results shouldHaveSize 5
    results.forEachIndexed { i, result ->
      result.message shouldBe null
      result.project!!.name shouldBe projectName[i]
    }
  }

  @Test
  fun `should report error if project is specified twice`() {
    // given

    // when
    val results = csvToProjects("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4022000131 -1";"50 - bewilligt";"very: important & club e.V.";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"Eva4everYeah";794;0;0;0;0;NA;"03,02,01";"17.01.2022";"01.07.2022"
      "4022000090 -1";"50 - bewilligt";"Über stringent society e.V.";345678;"Herr";"Max ";"Mustermann";"p3möglich@example.com";"Max; Out IT with Ã¤Ã¼Ã¶&20~";0;0;20;0;0;NA;"03,01,04";"09.03.2022";"09.03.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business  ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"Give ducks more rights";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4021000014 -2";"50 - bewilligt";"serious; business  ÖA GmbH";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"Give ducks more rights";3;4;457;0;0;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4022000005 -1";"50 - bewilligt";"Fun e.V. - We have Ätzend fun!";456789;"Herr";"Mäxi";"Mäxi";"p5tetrapack@example.com";"Amusement Park for ducks";NA;NA;NA;NA;NA;23;"01,07";"09.01.2022";"31.03.2022"
    """.trimIndent())

    // then
    results shouldHaveSize 5
    results[3].message shouldContain "CSV parsing error: duplicate project number"
  }

  @Test
  fun `should report error if CSV file contains incorrect data`() {
    // given
    val invalidEmail = "p1urtümlich@example@.com"
    val invalidStart = "17-01-2022"
    val invalidEnd = "01-07-2022"
    val nonValidWorker = "FOO"
    val age1to5 = "A"

    // when
    val results = csvToProjects("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "4021000014 -1";"50 - bewilligt";"invalid e-mail";123456;"Frau";"Maxi";"Musterfräulein";"$invalidEmail";"project1";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"
      "4022000131 -1";"50 - bewilligt";"invalid project.start";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"project2";794;0;0;0;0;NA;"03,02,01";"$invalidStart";"01.07.2022"
      "4022000132 -1";"50 - bewilligt";"invalid project.end";234567;"Frau";"Maxi";"Musterfrau";"p2gärung@example.net";"project2";794;0;0;0;0;NA;"03,02,01";"17.01.2022";"$invalidEnd"
      "4022000090 -1";"50 - bewilligt";"invalid participants.worker";345678;"Herr";"Max ";"Mustermann";"p3möglich@example.com";"project3";0;0;20;0;0;$nonValidWorker;"03,01,04";"09.03.2022";"09.03.2022"
      "4021000014 -2";"50 - bewilligt";"invalid participants.ageXtoY";123456;"Herr";"Mäxi";"Müstermän";"p4süß@example.org";"project4";$age1to5;B;C;D;E;NA;"04,03,01";"03.01.2022";"31.08.2022"
      "4022000005 -1";"50 - bewilligt";"invalid column count";456789;"Herr";"Mäxi";"Mäxi";"p5tetrapack@example.com";"project5"      
    """.trimIndent())

    // then
    results shouldHaveSize 6
    results[0].message shouldBe "CSV parsing error: invalid e-mail"
    results[1].message shouldBe "CSV parsing error: invalid start date"
    results[2].message shouldBe "CSV parsing error: invalid end date"
    results[3].message shouldBe "CSV parsing error: invalid worker"
    results[4].message shouldBe "CSV parsing error: invalid age1to5"
    results[5].message shouldContain "CSV parsing error: wrong column count"
  }

  @Test
  fun `should report error if project already exists`() {
    // given
    val id = "4021000014 -1"
    every { repository.existsById(id) } answers { true }

    // when
    val results = csvToProjects("""
      "this file ends with a dangling double quote character";"x";"x"
      "$id";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"expectedProjectName";0;0;250;50;0;NA;"01,05,03";"22.11.2021";"31.08.2022"    
    """.trimIndent())

    // then
    results shouldHaveSize 1
    val result = results[0]
    result.message shouldContain "already exists"
    result.project shouldBe null
  }
}
