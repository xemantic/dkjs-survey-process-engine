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

package de.dkjs.survey.model

import de.dkjs.survey.test.addTestActivity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SurveyProcessTest {

  @Test
  fun `should not assume that activity is already executed if no activities were executed so far`() {
    // given
    val process = SurveyProcess(id = "42", start = LocalDateTime.now(), phase = SurveyProcess.Phase.ACTIVE)

    // then
    process.alreadyExecuted("foo") shouldBe false
  }

  @Test
  fun `should not assume that activity is already executed if another activity was executed`() {
    // given
    val process = SurveyProcess(id = "42", start = LocalDateTime.now(), phase = SurveyProcess.Phase.ACTIVE)

    // when
    process.addTestActivity("foo")

    // then
    process.alreadyExecuted("bar") shouldBe false
  }

  @Test
  fun `should assume that activity is already executed if specified activity name matches`() {
    // given
    val process = SurveyProcess(id = "42", start = LocalDateTime.now(), phase = SurveyProcess.Phase.ACTIVE)

    // when
    process.addTestActivity("send REMINDER_1_T1")
    process.addTestActivity("send REMINDER_2_RETRO")

    // then
    process.alreadyExecuted("send REMINDER_1_T1") shouldBe true
  }

}
