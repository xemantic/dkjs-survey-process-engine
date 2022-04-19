/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.model

import de.dkjs.survey.mail.MailType
import de.dkjs.survey.test.addNotification
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SurveyProcessTest {

  @Test
  fun `should not assume that notification is already sent if no notifications were sent so far`() {
    // given
    val process = SurveyProcess(id = "42", phase = SurveyProcess.Phase.ACTIVE)

    // then
    process.isAlreadySent(setOf(MailType.INFOMAIL_RETRO)) shouldBe false
  }

  @Test
  fun `should not assume that notification is already sent if another mail types were sent`() {
    // given
    val process = SurveyProcess(id = "42", phase = SurveyProcess.Phase.ACTIVE)

    // when
    process.addNotification(MailType.INFOMAIL_RETRO)

    // then
    process.isAlreadySent(setOf(MailType.REMINDER_1_T1)) shouldBe false
  }

  @Test
  fun `should assume that notification is already sent if specified mail type matches`() {
    // given
    val process = SurveyProcess(id = "42", phase = SurveyProcess.Phase.ACTIVE)

    // when
    process.addNotification(MailType.REMINDER_1_T1)
    process.addNotification(MailType.REMINDER_2_RETRO)

    // then
    process.isAlreadySent(setOf(MailType.REMINDER_1_T1)) shouldBe true
  }

  @Test
  fun `should assume that notification is already sent if specified mail type matches multipe types`() {
    // given
    val process = SurveyProcess(id = "42", phase = SurveyProcess.Phase.ACTIVE)

    // when
    process.addNotification(MailType.REMINDER_1_T1)
    process.addNotification(MailType.REMINDER_2_RETRO)

    // then
    process.isAlreadySent(setOf(MailType.REMINDER_1_T1, MailType.REMINDER_2_RETRO)) shouldBe true
  }

}
