/*
 * Copyright (c) 2021-2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.documents

import de.dkjs.survey.model.SurveyType
import de.dkjs.survey.test.projectWithGoals
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SurveyPdfDocumentsLinkGeneratorTest {

  private val config = DocumentsConfig(
    urlBase = "https://www.auf-leben.org/evaluation/"
  )

  private val generator = SurveyPdfDocumentsLinkGenerator(config)

  @ParameterizedTest
  @CsvSource(
    value = [
    "1-2  , IMPULS,  https://www.auf-leben.org/evaluation/imp-ab",
    "1-3  , IMPULS,  https://www.auf-leben.org/evaluation/imp-ac",
    "1-4  , IMPULS,  https://www.auf-leben.org/evaluation/imp-ad",
    "1-5  , IMPULS,  https://www.auf-leben.org/evaluation/imp-ae",
    "1-6  , IMPULS,  https://www.auf-leben.org/evaluation/imp-af",
    "1-2-3, IMPULS,  https://www.auf-leben.org/evaluation/imp-abc",
    "1-2-4, IMPULS,  https://www.auf-leben.org/evaluation/imp-abd",
    "1-2-5, IMPULS,  https://www.auf-leben.org/evaluation/imp-abe",
    "1-2-6, IMPULS,  https://www.auf-leben.org/evaluation/imp-abf",
    "1-3-4, IMPULS,  https://www.auf-leben.org/evaluation/imp-acd",
    "1-3-5, IMPULS,  https://www.auf-leben.org/evaluation/imp-ace",
    "1-3-6, IMPULS,  https://www.auf-leben.org/evaluation/imp-acf",
    "1-4-5, IMPULS,  https://www.auf-leben.org/evaluation/imp-ade",
    "1-4-6, IMPULS,  https://www.auf-leben.org/evaluation/imp-adf",
    "1-5-6, IMPULS,  https://www.auf-leben.org/evaluation/imp-aef",
    "1-2  , PRE,     https://www.auf-leben.org/evaluation/pre-ab",
    "1-3  , PRE,     https://www.auf-leben.org/evaluation/pre-ac",
    "1-4  , PRE,     https://www.auf-leben.org/evaluation/pre-ad",
    "1-5  , PRE,     https://www.auf-leben.org/evaluation/pre-ae",
    "1-6  , PRE,     https://www.auf-leben.org/evaluation/pre-af",
    "1-2-3, PRE,     https://www.auf-leben.org/evaluation/pre-abc",
    "1-2-4, PRE,     https://www.auf-leben.org/evaluation/pre-abd",
    "1-2-5, PRE,     https://www.auf-leben.org/evaluation/pre-abe",
    "1-2-6, PRE,     https://www.auf-leben.org/evaluation/pre-abf",
    "1-3-4, PRE,     https://www.auf-leben.org/evaluation/pre-acd",
    "1-3-5, PRE,     https://www.auf-leben.org/evaluation/pre-ace",
    "1-3-6, PRE,     https://www.auf-leben.org/evaluation/pre-acf",
    "1-4-5, PRE,     https://www.auf-leben.org/evaluation/pre-ade",
    "1-4-6, PRE,     https://www.auf-leben.org/evaluation/pre-adf",
    "1-5-6, PRE,     https://www.auf-leben.org/evaluation/pre-aef",
    "1-2  , POST,    https://www.auf-leben.org/evaluation/repo-ab",
    "1-3  , POST,    https://www.auf-leben.org/evaluation/repo-ac",
    "1-4  , POST,    https://www.auf-leben.org/evaluation/repo-ad",
    "1-5  , POST,    https://www.auf-leben.org/evaluation/repo-ae",
    "1-6  , POST,    https://www.auf-leben.org/evaluation/repo-af",
    "1-2-3, POST,    https://www.auf-leben.org/evaluation/repo-abc",
    "1-2-4, POST,    https://www.auf-leben.org/evaluation/repo-abd",
    "1-2-5, POST,    https://www.auf-leben.org/evaluation/repo-abe",
    "1-2-6, POST,    https://www.auf-leben.org/evaluation/repo-abf",
    "1-3-4, POST,    https://www.auf-leben.org/evaluation/repo-acd",
    "1-3-5, POST,    https://www.auf-leben.org/evaluation/repo-ace",
    "1-3-6, POST,    https://www.auf-leben.org/evaluation/repo-acf",
    "1-4-5, POST,    https://www.auf-leben.org/evaluation/repo-ade",
    "1-4-6, POST,    https://www.auf-leben.org/evaluation/repo-adf",
    "1-5-6, POST,    https://www.auf-leben.org/evaluation/repo-aef",
    "1-7  , PRE ,    https://www.auf-leben.org/evaluation/qualipre",
    "1-7  , POST,    https://www.auf-leben.org/evaluation/qualirepo"
  ])
  fun `should generate survey PDF documents links`(goals: String, surveyType: SurveyType, url: String) {
    val goalNumbers = goals.split("-").map { it.toInt() }
    generator.generate(projectWithGoals(goalNumbers), surveyType) shouldBe url
  }

}
