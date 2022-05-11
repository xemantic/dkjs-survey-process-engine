/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.mail.MailType
import de.dkjs.survey.model.*
import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import de.dkjs.survey.test.SurveyProcessTestBase
import de.dkjs.survey.time.dkjsDateTime
import io.mockk.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@DkjsSurveyProcessEngineTest
class SurveyProcessTest : SurveyProcessTestBase() {

  @Test
  fun `test case 1 - project data gets into the system before the project starts`() {
    // given
    val projectId = "test case 1"
    val start = now() + 5.days
    val end = start + 10.days
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 2 - project data gets into the system, the project has already started, but it didn't end yet`() {
    // given
    val projectId = "test case 2"
    val now = now()
    val start = now - 1.days
    val end = now + 12.days
    numberOfSurveyResponses(SurveyType.POST, 42)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_RETRO, SurveyType.POST)
    }
  }

  // TODO Alex - is REMINDIR_1_RETRO unconditional? (as on the 1st digram?)
  /*
  Matchers:
+SurveyEmailSender(defaultSurveyEmailSender bean#1).send(any(), eq(INFOMAIL_RETRO), eq(POST)))
+SurveyEmailSender(defaultSurveyEmailSender bean#1).send(any(), eq(REMINDER_1_RETRO), eq(POST)))
SurveyEmailSender(defaultSurveyEmailSender bean#1).send(any(), eq(REMINDER_2_RETRO), eq(POST)))
+AlertSender(defaultAlertSender bean#2).sendProcessAlert(eq(No survey responses received 2 weeks after project ended), any()))

Calls:
1) +SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@657ddd2b, INFOMAIL_RETRO, POST)
2) +SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@22e469b7, REMINDER_1_RETRO, POST)
3) +AlertSender(defaultAlertSender bean#2).sendProcessAlert(No survey responses received 2 weeks after project ended, de.dkjs.survey.model.Project@445057fd)

   */
  @Test
  fun `test case 2a - project data gets into the system, the project has already started, but it didn't end yet, no answers`() {
    // given
    val projectId = "test case 2a"
    val now = now()
    val start = now - 1.days
    val end = now + 12.days
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  // TODO Alex - how about this test case?
  @Test
  @Disabled
  fun `test case 3 - project data gets into the system, there are more than two weeks till the project end (this can also be before the project starts), but it didn't end yet`() {
    // given
    val projectId = "test case 3"
    val now = now()
    val start = now - 1.days
    val end = now + 15.days
    numberOfSurveyResponses(SurveyType.POST, 0)


    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 4 - project data gets into the system, the project has already ended, but not more than one week elapsed after that and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 4"
    val now = now()
    val start = now - 10.days
    val end = now - 5.days
    numberOfSurveyResponses(SurveyType.POST, 0)


    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 5 - project data gets into the system, the project ended more than a week ago, but not yet two weeks and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 5"
    val now = now()
    val start = now - 10.days
    val end = now - 8.days
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  // TODO ask Alex - in excel description it is 1 week, in testing criteria 2 weeks
  @Test
  fun `test case 6 - project data gets into the system, the project ended more than 2 weeks ago and there is no typeform data`() {
    // given
    val projectId = "test case 6"
    val end = now() - 15.days
    val start = end - 20.days
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 7 - project data gets into the system more than a week before it starts, only PRE typeform surveys 1 week after project start`() {
    // given
    val projectId = "test case 7"
    val start = now() + 8.days
    val end = start + 15.days
    numberOfSurveyResponses(SurveyType.PRE, 42)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T0, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1, SurveyType.POST)
      // TODO Alex - should we differenciate PRE and POST survey count?
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  // TODO Alex - should it switch to RETRO scenario?
  /*
  Matchers:
+SurveyEmailSender(defaultSurveyEmailSender bean#1).send(any(), eq(INFOMAIL_PRE_POST), eq(PRE)))
+SurveyEmailSender(defaultSurveyEmailSender bean#1).send(any(), eq(REMINDER_1_T0), eq(PRE)))
+SurveyEmailSender(defaultSurveyEmailSender bean#1).send(any(), eq(REMINDER_2_T0), eq(PRE)))
+AlertSender(defaultAlertSender bean#2).sendProcessAlert(eq(No survey responses received 2 weeks after project ended), any()))

Calls:
1) +SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@4e7c50cf, INFOMAIL_PRE_POST, PRE)
2) +SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@7116af4c, REMINDER_1_T0, PRE)
3) +SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@5177c674, REMINDER_2_T0, PRE)
4) AlertSender(defaultAlertSender bean#2).sendProcessAlert(No survey responses (data t0) 2 weeks after project starts, de.dkjs.survey.model.Project@1bdba03b)
5) SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@6c4c0046, REMINDER_1_T1, POST)
6) +AlertSender(defaultAlertSender bean#2).sendProcessAlert(No survey responses received 2 weeks after project ended, de.dkjs.survey.model.Project@424b5acc)
   */
  @Test
  fun `test case 7a - project data gets into the system more than a week before it starts, no typeform surveys 1 week after project start`() {
    // given
    val projectId = "test case 7a"
    val start = now() + 8.days
    val end = start + 15.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T0, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_2_T0, SurveyType.PRE)
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 7b - project data gets into the system more than a week before it starts, no typeform surveys 1 week after project start`() {
    // given
    val projectId = "test case 7a"
    val start = now() + 8.days
    val end = start + 15.days
    numberOfSurveyResponses(SurveyType.PRE, 42)
    numberOfSurveyResponses(SurveyType.POST, 24)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T0, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.POST)
    }
  }

  // TODO Alex - if there is no data, should it send retro survey?
  /*

  Matchers:
+SurveyEmailSender(defaultSurveyEmailSender bean#1).send(any(), eq(INFOMAIL_PRE_POST), eq(PRE)))
+SurveyEmailSender(defaultSurveyEmailSender bean#1).send(any(), eq(REMINDER_1_T0), eq(PRE)))
+SurveyEmailSender(defaultSurveyEmailSender bean#1).send(any(), eq(REMINDER_2_T0), eq(PRE)))
+AlertSender(defaultAlertSender bean#2).sendProcessAlert(eq(No survey responses received 2 weeks after project ended), any()))

Calls:
1) +SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@3a5b8d2e, INFOMAIL_PRE_POST, PRE)
2) +SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@11fe8d35, REMINDER_1_T0, PRE)
3) +SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@7c39cb92, REMINDER_2_T0, PRE)
4) +AlertSender(defaultAlertSender bean#2).sendProcessAlert(No survey responses (data t0) 2 weeks after project starts, de.dkjs.survey.model.Project@4bbc731e)
5) SurveyEmailSender(defaultSurveyEmailSender bean#1).send(de.dkjs.survey.model.Project@5e354c44, REMINDER_1_T1, POST)
6) +AlertSender(defaultAlertSender bean#2).sendProcessAlert(No survey responses received 2 weeks after project ended, de.dkjs.survey.model.Project@8f622f)

   */
  @Test
  fun `test case 8 - project data gets into the system and there is a week or less until it starts`() {
    // given
    val projectId = "test case 8"
    val start = now() + 6.days
    val end = start + 15.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T0, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_2_T0, SurveyType.PRE)
      alertSender.sendProcessAlert("No survey responses (data t0) 2 weeks after project starts", any())
    }
  }

  // TODO Alex - will be fixed with RETRO process definition fix
  @Test
  fun `test case 9 - project data gets into the system, the project has a duration of exactly 14 days, already started but at maximum one week ago and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 9"
    val end = now() + 9.days
    val start = end - 14.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)


    println("!!!!projectStart: $start")
    println("!!!!projectEnd: $end")

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 10 - project data gets into the system, the project has a duration of exactly 14 days, already started but ended at maximum one week ago and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 10"
    val end = now() + 6.days
    val start = end - 14.days

    println("!!!!projectStart: $start")
    println("!!!!projectEnd: $end")


    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 11 - project data gets into the system, the project has a duration of exactly 14 days, already started and ended at least two weeks ago and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 11"
    val now = now()
    val end = now - 15.days
    val start = end - 14.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 12 - project data gets into the system, the project has a duration of over 14 days, already started but not longer than one week ago and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 12"
    val now = now()
    val start = now - 5.days
    val end = now + 10.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T0, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_2_T0, SurveyType.PRE)
      alertSender.sendProcessAlert("No survey responses (data t0) 2 weeks after project starts", any())
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 13 - project data gets into the system, the project has a duration of over 14 days, already started more than one week ago but still has a week to go until it ends and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 13"
    val now = now()
    val start = now - 10.days
    val end = now + 6.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_2_T0, SurveyType.PRE)
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 14 - project data gets into the system, the project has a duration of over 14 days, already started more than one week ago but still has a week to go until it ends and typeform data has been entered `() {
    // given
    val projectId = "test case 14"
    val now = now()
    val start = now - 10.days
    val end = now + 6.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA // one day before GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.PRE)
    }
  }

  @Test
  fun `test case 15 - project data gets into the system, the project has a duration of over 14 days, has less than one week until it ends and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 15"
    val now = now()
    val end = now + 5.days
    val start = end - 15.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_RETRO, SurveyType.POST)
    }
  }

  @Test
  fun `test case 16 - project data gets into the system, the project has a duration of over 14 days, has ended not more than one week ago and no t1 typeform data has been entered yet`() {
    // given
    val projectId = "test case 16"
    val now = now()
    val end = now - 5.days
    val start = end - 15.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(any(), MailType.INFOMAIL_RETRO, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)
    }
  }

  @Test
  fun `test case 17 - project data gets into the system, the project has a duration of over 14 days and ended more than two weeks ago and no t1 typeform data has been entered yet`() {
    // given
    val projectId = "test case 17"
    val now = now()
    val end = now - 15.days
    val start = end - 15.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

}
