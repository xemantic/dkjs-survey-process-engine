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

class CompressedTimeConstraintsTest {

  @Test
  fun `should calculate duration of 1 day compressed to 1 second for 1 second difference`() {
    // given
    val start = LocalDateTime.of(2022, 5,9, 1, 10, 0)
    val end = LocalDateTime.of(2022, 5,9, 1, 10, 1)
    val timeConstraints = CompressedTimeConstraints(
      start,
      end,
      TimeConfig(
        dayDurationAsNumberOfSeconds = 1,
        timeZone = "Europe/Berlin"
      )
    )

    // then
    timeConstraints.projectDurationInDays shouldBe 1
  }

  @Test
  fun `should calculate duration of 1 days compressed to 2 seconds for 2 seconds difference`() {
    // given
    val start = LocalDateTime.of(2022, 5,9, 1, 10, 0)
    val end = LocalDateTime.of(2022, 5,9, 1, 10, 2)
    val timeConstraints = CompressedTimeConstraints(
      start,
      end,
      TimeConfig(
        dayDurationAsNumberOfSeconds = 2,
        timeZone = "Europe/Berlin"
      )
    )

    // then
    timeConstraints.projectDurationInDays shouldBe 1
  }

  @Test
  fun `should calculate duration of 14 days compressed to 1 second per day for 14 seconds difference`() {
    // given
    val start = LocalDateTime.of(2022, 5,9, 1, 10, 0)
    val end = LocalDateTime.of(2022, 5,9, 1, 10, 14)
    val timeConstraints = CompressedTimeConstraints(
      start,
      end,
      TimeConfig(
        dayDurationAsNumberOfSeconds = 1,
        timeZone = "Europe/Berlin"
      )
    )

    // then
    timeConstraints.projectDurationInDays shouldBe 14
  }

  @Test
  fun `should calculate duration of 14 days compressed to 2 seconds per day for 28 seconds difference`() {
    // given
    val start = LocalDateTime.of(2022, 5,9, 1, 10, 0)
    val end = LocalDateTime.of(2022, 5,9, 1, 10, 28)
    val timeConstraints = CompressedTimeConstraints(
      start,
      end,
      TimeConfig(
        dayDurationAsNumberOfSeconds = 2,
        timeZone = "Europe/Berlin"
      )
    )

    // then
    timeConstraints.projectDurationInDays shouldBe 14
  }

}
