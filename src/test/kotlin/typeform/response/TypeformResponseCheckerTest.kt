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

package de.dkjs.survey.typeform.response

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.SurveyType
import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@DkjsSurveyProcessEngineTest
class TypeformResponseCheckerTest @Autowired constructor(
  private val checker: TypeformResponseChecker
) {

  @Test
  @Disabled // disabled as undine project disappeared from typeform, can be enabled again if any POST project is added
  fun `should count existing responses in typeform for a given project`() {
    // given
    val project = mockk<Project>()
    every { project.id } returns "undine"
    every { project.goals } returns setOf(1, 3, 4)
    every { project.isGoalG } returns false

    // when
    val count = checker.countSurveys(project, SurveyType.POST)

    // then
    count shouldBeGreaterThan 0
  }

  @Test
  fun `should count 0 responses in typeform for non-existent project`() {
    // given
    val project = mockk<Project>()
    every { project.id } returns "non-existent project"
    every { project.goals } returns setOf(1, 3, 4)
    every { project.isGoalG } returns false

    // when
    val count = checker.countSurveys(project, SurveyType.POST)

    // then
    count shouldBe 0
  }

}
