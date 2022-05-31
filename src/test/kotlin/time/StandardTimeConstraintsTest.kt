/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
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
