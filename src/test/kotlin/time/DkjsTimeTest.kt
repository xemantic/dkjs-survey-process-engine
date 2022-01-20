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
  fun `should format date without time component for LocalDate instance`() {
    // given
    val moment = LocalDate.of(2022, 1, 20)

    // when
    val date = DKJS_DATE_FORMAT.format(moment)

    // then
    date shouldBe "2022.01.20"
  }

  @Test
  fun `should format date with time component for LocalDateTime instance`() {
    // given
    val moment = LocalDate.of(2022, 1, 20).atStartOfDay()

    // when
    val date = DKJS_DATE_FORMAT.format(moment)

    // then
    date shouldBe "2022.01.20 00:00:00"
  }

  @Test
  fun `should parse date with time component for LocalDateTime instance`() {
    // given
    val date = "2022.01.20 00:00:01"

    // when
    val moment = LocalDateTime.parse(date, DKJS_DATE_FORMAT)

    // then
    moment shouldBe LocalDateTime.of(2022, 1, 20, 0, 0, 1)
  }

}
