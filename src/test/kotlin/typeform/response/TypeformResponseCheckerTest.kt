/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.response

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.Scenario
import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

// TODO verify this test

@DkjsSurveyProcessEngineTest
class TypeformResponseCheckerTest @Autowired constructor(
  private val checker: TypeformResponseChecker
) {

  @Test
  fun `should count existing responses in typeform for a given project`() {
    // given
    val project = mockk<Project>()
    every { project.id } returns "undine"
    every { project.goals } returns listOf(1, 3, 4)

    // when
    val count = checker.countSurveys(project, Scenario.RETRO)

    // then
    count shouldBeGreaterThan 0
  }

  @Test
  fun `should count 0 responses in typeform for non-existent project`() {
    // given
    val project = mockk<Project>()
    every { project.id } returns "undine"
    every { project.goals } returns listOf(1, 3, 4)

    // when
    val count = checker.countSurveys(project, Scenario.RETRO)

    // then
    count shouldBe 0
  }

}
