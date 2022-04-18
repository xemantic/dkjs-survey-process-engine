/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.engine.defineProcess
import de.dkjs.survey.mail.MailType

/**
 * Defines the process triggered when scenario `PRE_POST` is detected.
 */
fun definePrePostProcess() = defineProcess {

  sendImmediately(MailType.INFOMAIL_PRE_POST)

  scheduleMailAt(time.oneWeekBeforeProjectStarts, MailType.REMINDER_1_T0)

  scheduleIfNotSent(
    time.oneWeekAfterProjectStarts,
    setOf(
      MailType.REMINDER_1_T1_RETRO,
      MailType.REMINDER_2_T0,
      MailType.INFOMAIL_T1
    )
  ) {
    if (hasNoAnswers()) {
      if (time.is14DaysProjectDuration) {
        send(MailType.REMINDER_1_T1_RETRO) // master all german post
      } else {
        send(MailType.REMINDER_2_T0) // master all German PRE
      }
    } else {
      scheduleAt(time.oneWeekBeforeProjectEnds) {
        send(MailType.INFOMAIL_T1) // master all german post
      }
    }
  }

  if (!time.is14DaysProjectDuration) {
    scheduleAt(time.twoWeeksAfterProjectStarts) {
      sendAlert("message") // TODO what should be the message?
    }
  }

  scheduleIfNotSent(
    time.oneWeekAfterProjectEnds,
    setOf(
      MailType.REMINDER_2_T1_RETRO,
      MailType.REMINDER_1_T1
    )
  ) {
    if (hasNoAnswers()) {
      if (time.is14DaysProjectDuration) {
        send(MailType.REMINDER_2_T1_RETRO) // master all german post
      } else {
        send(MailType.REMINDER_1_T1) // master all german post
      }
    }
  }

  scheduleAt(time.twoWeeksAfterProjectEnds) {
    if (hasNoAnswers()) {
      sendAlert("message") // TODO what should be the message?
    }
    finishProcess()
  }

}
