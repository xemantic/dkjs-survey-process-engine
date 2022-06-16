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

package de.dkjs.survey.time

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class StandardTimeConstraintsTest {

  @Test
  fun `should calculate duration of 1 day`() {
    // given
    val start = LocalDateTime.of(2022, 5, 1, 0, 0)
    val end = LocalDateTime.of(2022, 5, 2, 0, 0)
    val timeConstraints = StandardTimeConstraints(start, end)

    // then
    timeConstraints.projectDurationInDays shouldBe 1
  }

  @Test
  fun `should calculate duration of 14 days`() {
    // given
    val start = LocalDateTime.of(2022, 5, 1, 1, 0, 0)
    val end = LocalDateTime.of(2022, 5, 15, 1, 0, 0)
    val timeConstraints = StandardTimeConstraints(start, end)

    // then
    timeConstraints.projectDurationInDays shouldBe 14
  }

}
