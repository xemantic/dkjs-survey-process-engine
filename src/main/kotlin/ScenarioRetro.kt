/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.engine.defineProcess
import de.dkjs.survey.mail.MailType

/**
 * Defines the process triggered when scenario `RETRO` is detected.
 */
fun defineRetroProcess() = defineProcess {

  sendImmediately(MailType.REMINDER_1_RETRO)

  scheduleMailAt(time.oneWeekBeforeProjectEnds, MailType.REMINDER_1_RETRO)

  scheduleMailAt(time.oneWeekAfterProjectEnds, MailType.REMINDER_2_RETRO)

  scheduleAt(time.twoWeeksAfterProjectEnds) {
    if (hasNoAnswers()) {
      sendAlert("No surveys received 2 weeks after project ended")
    }
    finishProcess()
  }

}
