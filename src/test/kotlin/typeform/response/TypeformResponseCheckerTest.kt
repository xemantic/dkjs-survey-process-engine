/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.response

import de.dkjs.survey.model.ScenarioType
import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@DkjsSurveyProcessEngineTest
class TypeformResponseCheckerTest @Autowired constructor(
  private val checker: TypeformResponseChecker
) {

  @Test
  fun `should count existing responses in typeform for a given project`() {
    // given
    val projectId = "undine" // created during testing

    // when
    val count = checker.countSurveys(projectId, ScenarioType.PRE)

    // then
    count shouldBeGreaterThan 0
  }

  @Test
  fun `should count 0 responses in typeform for non-existent project`() {
    // given
    val projectId = "non-existent project"

    // when
    val count = checker.countSurveys(projectId, ScenarioType.PRE)

    // then
    count shouldBe 0
  }

}
