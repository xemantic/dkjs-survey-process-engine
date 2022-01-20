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
    date shouldBe "2022.01.20"
  }

  @Test
  fun `should format DKJS date without time component for LocalDateTime instance`() {
    // given
    val moment = LocalDateTime.of(2022, 1, 20, 1, 1, 1)

    // when
    val date = moment.dkjsDate

    // then
    date shouldBe "2022.01.20"
  }

  @Test
  fun `should format DKJS date with time component for LocalDateTime instance`() {
    // given
    val moment = LocalDateTime.of(2022, 1, 20, 1, 2, 3)

    // when
    val date = moment.dkjsDateTime

    // then
    date shouldBe "2022.01.20 01:02:03"
  }

  @Test
  fun `should parse date with time component for LocalDateTime instance`() {
    // given
    val date = "2022.01.20 03:02:01"

    // when
    val moment = parseDkjsDate(date)

    // then
    moment shouldBe LocalDateTime.of(2022, 1, 20, 3, 2, 1)
  }

}
