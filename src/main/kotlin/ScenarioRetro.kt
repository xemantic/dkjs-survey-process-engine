/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.engine.defineProcess
import de.dkjs.survey.mail.MailType

/**
 * Defines the process triggered when scenario `RETRO` is detected.
 *
 * Note: names of executed activities HAVE TO DIFFER!
 */
fun defineRetroProcess() = defineProcess {

  execute("send REMINDER_1_RETRO") {
    send(MailType.REMINDER_1_RETRO)
  }

  schedule(
    "send REMINDER_1_RETRO again 1 week before project ends",
    time.oneWeekBeforeProjectEnds
  ) {
    send(MailType.REMINDER_1_RETRO)
  }

  schedule(
    "send REMINDER_2_RETRO 1 week after project ends",
    time.oneWeekAfterProjectEnds
  ) {
    send(MailType.REMINDER_2_RETRO)
  }

  schedule(
    "end check",
    time.twoWeeksAfterProjectEnds
  ) {
    if (hasNoAnswers()) {
      sendAlert("No surveys received 2 weeks after project ended")
    }
    finishProcess()
  }

}
