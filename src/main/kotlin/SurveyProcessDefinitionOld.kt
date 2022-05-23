/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.engine.defineProcess
import de.dkjs.survey.mail.MailType
import de.dkjs.survey.model.SurveyType

/**
 * Defines the survey process.
 *
 * Note: names of executed activities HAVE TO DIFFER!
 */
// TODO to be removed, just for reference until new processDefintionIsFinished
fun defineSurveyProcessOld() = defineProcess {

  if (processStart.isBefore(time.twoWeeksAfterProjectEnds)) {

    // || processStart.isAfter(time.oneWeekBeforeProjectEnds)
    if ((time.projectDurationInDays < 14)) {

      // RETRO

      execute("send INFOMAIL_RETRO") {
        send(MailType.INFOMAIL_RETRO, SurveyType.POST)
      }

      if (processStart.isBefore(time.oneWeekAfterProjectStarts)) {
        schedule(
          "send REMINDER_1_RETRO 1 week before project ends",
          time.oneWeekBeforeProjectEnds
        ) {
          send(MailType.REMINDER_1_RETRO, SurveyType.POST)
        }
      }

      if (processStart.isAfter(time.oneWeekAfterProjectStarts)) {
        schedule(
          "send REMINDER_2_RETRO 1 week after project ends",
          time.oneWeekAfterProjectEnds
        ) {
          if (hasNoSurveyResponses(SurveyType.POST)) {
            send(MailType.REMINDER_2_RETRO, SurveyType.POST)
          } else {
            "Not sending REMINDER_2_RETRO because POST survey has responses"
          }
        }
      }

    } else {

      execute("send INFOMAIL_PRE_POST") {
        send(MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      }

      if (processStart.isBefore(time.oneWeekAfterProjectStarts)) {
        schedule(
          "send REMINDER_1_T0 one week before project starts",
          time.oneWeekBeforeProjectStarts
        ) {
          send(MailType.REMINDER_1_T0, SurveyType.PRE)
        }
      }

      scheduleCheck(
        "check survey response 1 week after project starts",
        time.oneWeekAfterProjectStarts,
        check = { hasSurveyResponses(SurveyType.PRE) },
        onTrue = {

          schedule(
            "send INFOMAIL_T1 1 week before project ends",
            time.oneWeekBeforeProjectEnds
          ) {
            send(MailType.INFOMAIL_T1, SurveyType.POST)
          }

          if (processStart.isBefore(time.oneWeekAfterProjectStarts)) {
            schedule(
              "send REMINDER_1_T0 1 week after project ends",
              time.oneWeekAfterProjectEnds
            ) {
              if (hasNoSurveyResponses(SurveyType.PRE)) {
                send(MailType.REMINDER_1_T0, SurveyType.PRE)
              } else {
                "Not sending REMINDER_1_T0 because POST survey has responses"
              }
            }
          }

        },
        onFalse = {

          if (time.projectDurationInDays > 14) {

            if (processStart.isBefore(time.oneWeekAfterProjectStarts)) {
              schedule(
                "send REMINDER_1_T0 1 week after project ends",
                time.oneWeekAfterProjectEnds
              ) {
                if (hasNoSurveyResponses(SurveyType.PRE)) {
                  send(MailType.REMINDER_1_T0, SurveyType.PRE)
                } else {
                  "Not sending REMINDER_1_T0 because POST survey has responses"
                }
              }
            } else {
              schedule(
                "send REMINDER_2_T0 1 week after project ends",
                time.oneWeekAfterProjectEnds
              ) {
                if (hasNoSurveyResponses(SurveyType.PRE)) {
                  send(MailType.REMINDER_2_T0, SurveyType.PRE)
                } else {
                  "Not sending REMINDER_2_T0 because POST survey has responses"
                }
              }
            }

            schedule(
              "send alert if no survey responses (data t0) 2 weeks after project starts",
              time.twoWeeksAfterProjectStarts
            ) {
              if (hasNoSurveyResponses(SurveyType.PRE)) {
                sendAlert("No survey responses (data t0) 2 weeks after project starts")
              } else {
                "Not sending alert because PRE survey has responses"
              }
            }

//            schedule(
//              "send REMINDER_1_T1 1 week after project ends",
//              time.oneWeekAfterProjectEnds
//            ) {
//              if (hasNoSurveyResponses(SurveyType.POST)) {
//                send(MailType.REMINDER_1_T1, SurveyType.POST)
//              } else {
//                "Not sending REMINDER_1_T1 because POST survey has responses"
//              }
//            }

          } else { // = 14 days

            execute("send REMINDER_1_T1_RETRO 1 week after project starts") {
              send(MailType.REMINDER_1_T1_RETRO, SurveyType.POST)
            }

            schedule(
              "send REMINDER_2_T1_RETRO 1 week after project ends",
              time.oneWeekAfterProjectEnds
            ) {
              if (hasNoSurveyResponses(SurveyType.POST)) {
                send(MailType.REMINDER_2_T1_RETRO, SurveyType.POST)
              } else {
                "Not sending REMINDER_2_T1_RETRO because POST survey has responses"
              }
            }

          }

        }
      )

    }

  }

  schedule(
    "end check",
    time.twoWeeksAfterProjectEnds
  ) {
    if (hasNoSurveyResponses(SurveyType.POST)) { // TODO Alex - but which surveys should be counted?
      sendAlert("No survey responses received 2 weeks after project ended")
    }
    finishProcess()
  }

}
