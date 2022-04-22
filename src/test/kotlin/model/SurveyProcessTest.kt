/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.model

import de.dkjs.survey.test.addTestActivity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SurveyProcessTest {

  @Test
  fun `should not assume that activity is already executed if no activities were executed so far`() {
    // given
    val process = SurveyProcess(id = "42", phase = SurveyProcess.Phase.ACTIVE)

    // then
    process.alreadyExecuted("foo") shouldBe false
  }

  @Test
  fun `should not assume that activity is already executed if another activity was executed`() {
    // given
    val process = SurveyProcess(id = "42", phase = SurveyProcess.Phase.ACTIVE)

    // when
    process.addTestActivity("foo")

    // then
    process.alreadyExecuted("bar") shouldBe false
  }

  @Test
  fun `should assume that activity is already executed if specified activity name matches`() {
    // given
    val process = SurveyProcess(id = "42", phase = SurveyProcess.Phase.ACTIVE)

    // when
    process.addTestActivity("send REMINDER_1_T1")
    process.addTestActivity("send REMINDER_2_RETRO")

    // then
    process.alreadyExecuted("send REMINDER_1_T1") shouldBe true
  }

}
