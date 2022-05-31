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
 * Note: names of executed activities HAVE TO DIFFER! execute(unique1), schedule(unique2) etc.
 */
fun defineSurveyProcess() = defineProcess {

  if (processStart.isBefore(time.twoWeeksAfterProjectEnds)) {

    if (time.projectDurationInDays < 15) {

      execute("send INFOMAIL_RETRO") {
        send(MailType.INFOMAIL_RETRO, SurveyType.POST)
      }


      // TODO Alex, is it a right condition, no specified on the diagram?
      if (processStart.isBefore(time.oneWeekBeforeProjectEnds)) {
        schedule(
          "send REMINDER_1_RETRO 1 week before project ends",
          time.oneWeekBeforeProjectEnds
        ) {
          send(MailType.REMINDER_1_RETRO, SurveyType.POST)
        }
      }

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
//    else if (time.projectDurationInDays == 14) {
//
//      execute("send INFOMAIL_RETRO") {
//        send(MailType.INFOMAIL_RETRO, SurveyType.POST)
//      }
//
//      println("!!!!processStart: $processStart")
//      println("!!!!oneWeekBeforeProjectEnds: ${time.oneWeekBeforeProjectEnds}")
//
//      if (processStart.isAfter(time.oneWeekBeforeProjectEnds)) {
//        schedule(
//          "send REMINDER_1_RETRO one week after project starts",
//          time.oneWeekBeforeProjectStarts
//        ) {
//          send(MailType.REMINDER_1_RETRO, SurveyType.POST)
//        }
//      }
//
//      schedule(
//        "send REMINDER_2_RETRO 1 week after project ends",
//        time.oneWeekAfterProjectEnds
//      ) {
//        if (hasNoSurveyResponses(SurveyType.POST)) {
//          send(MailType.REMINDER_2_RETRO, SurveyType.POST)
//        } else {
//          "Not sending REMINDER_2_RETRO because POST survey has responses"
//        }
//      }
//
////      scheduleCheck(
////        "check survey response 1 week after project starts",
////        time.oneWeekAfterProjectStarts,
////        check = { hasSurveyResponses(SurveyType.PRE) },
////        onTrue = {
////
////          schedule(
////            "send INFOMAIL_T1 1 week before project ends",
////            time.oneWeekBeforeProjectEnds
////          ) {
////            send(MailType.INFOMAIL_T1, SurveyType.POST)
////          }
////
////          if (processStart.isBefore(time.oneWeekAfterProjectStarts)) {
////
////          }
////
////        },
////        onFalse = {
////
////          execute("send REMINDER_1_T1_RETRO 1 week after project starts") {
////            send(MailType.REMINDER_1_T1_RETRO, SurveyType.POST)
////          }
////
////          schedule(
////            "send REMINDER_2_T1_RETRO 1 week after project ends",
////            time.oneWeekAfterProjectEnds
////          ) {
////            if (hasNoSurveyResponses(SurveyType.POST)) {
////              send(MailType.REMINDER_2_T1_RETRO, SurveyType.POST)
////            } else {
////              "Not sending REMINDER_2_T1_RETRO because POST survey has responses"
////            }
////          }
////
////        }
////      )
//
//    }
    else { // >14

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


        }
      )

    }

  }

  schedule(
    "end check",
    time.twoWeeksAfterProjectEnds
  ) {
    if (hasNoSurveyResponses(SurveyType.POST)) {
      sendAlert("No survey responses received 2 weeks after project ended")
    }
    finishProcess()
  }

}
