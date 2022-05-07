/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.engine.defineProcess
import de.dkjs.survey.mail.MailType

/**
 * Defines the process triggered when scenario `PRE_POST` is detected.
 *
 * Note: names of executed activities HAVE TO DIFFER!
 */
fun definePrePostProcess() = defineProcess {

  if (time.isMoreThan14DaysProjectDuration && processStart.isAfter(time.oneWeekBeforeProjectEnds)) {

    // RETRO

    execute("send INFOMAIL_RETRO") {
      send(MailType.INFOMAIL_RETRO)
    }

    if (processStart.isBefore(time.oneWeekBeforeProjectEnds)) {
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

  } else {

    if (time.isMoreThan14DaysProjectDuration) { // PRE_POST

      execute("send INFOMAIL_PRE_POST") {
        send(MailType.INFOMAIL_PRE_POST)
      }

      schedule(
        "send REMINDER_1_T0 one week before project starts",
        time.oneWeekBeforeProjectStarts
      ) {
        send(MailType.REMINDER_1_T0)
        finishProcess()
      }

    }
  }




//    if (processStart.isAfter(projectStart)) {
//
//      if (time.isMoreThan14DaysProjectDuration) {
//
//      } else {
//      }
//
//    }




//    schedule(
//      "actions after 1 week",
//      time.oneWeekAfterProjectStarts
//    ) {
//      if (hasNoAnswers()) {
//        if (time.is14DaysProjectDuration) {
//          send(MailType.REMINDER_1_T1_RETRO) // master all german post
//        } else {
//          send(MailType.REMINDER_2_T0) // master all German PRE
//        }
//      } else {
//        schedule("send INFOMAIL_T1", time.oneWeekBeforeProjectEnds) {
//          send(MailType.INFOMAIL_T1) // master all german post
//        }
//        "Scheduled INFOMAIL_T1"
//      }
//    }
//
//    if (!time.is14DaysProjectDuration) {
//      schedule(
//        "actions if project longer than 14 days",
//        time.twoWeeksAfterProjectStarts
//      ) {
//        sendAlert("No surveys received 2 weeks after project started")
//      }
//    }
//
//    schedule("actions after project ends", time.oneWeekAfterProjectEnds) {
//      if (hasNoAnswers()) {
//        if (time.is14DaysProjectDuration) {
//          send(MailType.REMINDER_2_T1_RETRO) // master all german post
//        } else {
//          send(MailType.REMINDER_1_T1) // master all german post
//        }
//      } else {
//        "Has survey answers, no action performed"
//      }
//    }
//
//    schedule("end check", time.twoWeeksAfterProjectEnds) {
//      val noAnswers = if (hasNoAnswers()) {
//        sendAlert("No surveys received 2 weeks after project ended")
//      } else {
//        ""
//      }
//      finishProcess() + " - " + noAnswers
//    }
//
//  }

}
