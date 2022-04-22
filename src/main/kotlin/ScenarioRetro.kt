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

  if (processStart.isAfter(time.twoWeeksAfterProjectEnds)) {

    execute("send alert if now is 2 weeks after project ends") {
      sendAlert("Project submitted 2 weeks after it ends")
      finishProcess()
    }

  } else {

    execute("send INFOMAIL_RETRO") {
      send(MailType.INFOMAIL_RETRO)
    }

    if (processStart.isBefore(time.oneWeekAfterProjectEnds)) {
      schedule(
        "send REMINDER_1_RETRO again 1 week before project ends",
        time.oneWeekBeforeProjectEnds
      ) {
        send(MailType.REMINDER_1_RETRO)
      }
    }

    schedule(
      "send REMINDER_2_RETRO 1 week after project ends",
      time.oneWeekAfterProjectEnds
    ) {
      if (hasNoAnswers()) {
        send(MailType.REMINDER_2_RETRO)
      } else {
        "Not sending REMINDER_2_RETRO because survey has answers"
      }
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

}
