/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
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
