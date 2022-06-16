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
import java.time.LocalDate
import java.time.LocalDateTime

class DkjsTimeTest {

  @Test
  fun `should format DKJS date without time component for LocalDate instance`() {
    // given
    val moment = LocalDate.of(2022, 1, 20)

    // when
    val date = moment.dkjsDate

    // then
    date shouldBe "20.01.2022"
  }

  @Test
  fun `should format DKJS date without time component for LocalDateTime instance`() {
    // given
    val moment1 = LocalDateTime.of(2022, 1, 20, 0, 0, 0)
    val moment2 = LocalDateTime.of(2022, 1, 20, 1, 1, 1)
    val moment3 = LocalDateTime.of(2022, 1, 20, 23, 59, 59)

    // when
    val date1 = moment1.dkjsDate
    val date2 = moment2.dkjsDate
    val date3 = moment3.dkjsDate

    // then
    date1 shouldBe "20.01.2022"
    date2 shouldBe "20.01.2022"
    date3 shouldBe "20.01.2022"
  }

  @Test
  fun `should format DKJS date with time component for LocalDateTime instance`() {
    // given
    val moment = LocalDateTime.of(2022, 1, 20, 1, 2, 3)

    // when
    val date = moment.dkjsDateTime

    // then
    date shouldBe "20.01.2022 01:02:03"
  }

  @Test
  fun `should parse date with time component for LocalDateTime instance`() {
    // given
    val date = "20.01.2022 03:02:01"

    // when
    val moment = parseDkjsDate(date)

    // then
    moment shouldBe LocalDateTime.of(2022, 1, 20, 3, 2, 1)
  }

  @Test
  fun `should parse date without time component for LocalDateTime instance`() {
    // given
    val date = "20.01.2022"

    // when
    val moment = parseDkjsDate(date)

    // then
    moment shouldBe LocalDateTime.of(2022, 1, 20, 0, 0, 0)
  }

}
