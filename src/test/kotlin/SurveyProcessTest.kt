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
    val now = now()
    val start = now.plusSeconds(5) // one day before
    val end = start.plusSeconds(10)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_1_RETRO, Scenario.RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_2_RETRO, Scenario.RETRO, any())
      alertSender.sendProcessAlert("No surveys received 2 weeks after project ended", any(), Scenario.RETRO)
    }
  }

  @Test
  fun `test case 2 - project data gets into the system, the project has already started, but it didn't end yet`() {
    // given
    val projectId = "test case 2"
    val now = now()
    val start = now.minusSeconds(1) // one day before
    val end = now.plusSeconds(12)
    numberOfFilledSurveys(42)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_1_RETRO, Scenario.RETRO, any())
    }
  }

  @Test
  fun `test case 2a - project data gets into the system, the project has already started, but it didn't end yet, no answers`() {
    // given
    val projectId = "test case 2a"
    val now = now()
    val start = now.minusSeconds(1) // one day before
    val end = now.plusSeconds(12)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_1_RETRO, Scenario.RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_2_RETRO, Scenario.RETRO, any())
      alertSender.sendProcessAlert("No surveys received 2 weeks after project ended", any(), Scenario.RETRO)
    }
  }

  @Test
  @Disabled
  fun `test case 3 - project data gets into the system, there are more than two weeks till the project end (this can also be before the project starts), but it didn't end yet`() {
    // given
    val projectId = "test case 2"
    val now = now()
    val start = now.minusSeconds(1) // one day before
    val end = now.plusSeconds(15)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_1_RETRO, Scenario.RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_2_RETRO, Scenario.RETRO, any())
      alertSender.sendProcessAlert("No surveys received 2 weeks after project ended", any(), Scenario.RETRO)
    }
  }

  @Test
  fun `test case 4 - project data gets into the system, the project has already ended, but not more than one week elapsed after that and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 4"
    val now = now()
    val start = now.minusSeconds(10) // one day before
    val end = now.minusSeconds(5)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_2_RETRO, Scenario.RETRO, any())
      alertSender.sendProcessAlert("No surveys received 2 weeks after project ended", any(), Scenario.RETRO)
    }
  }

  @Test
  fun `test case 5 - project data gets into the system, the project ended more than a week ago, but not yet two weeks and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 5"
    val now = now()
    val start = now.minusSeconds(10) // one day before
    val end = now.minusSeconds(8)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.RETRO, any())
      surveyEmailSender.send(MailType.REMINDER_2_RETRO, Scenario.RETRO, any())
      alertSender.sendProcessAlert("No surveys received 2 weeks after project ended", any(), Scenario.RETRO)
    }
  }

  @Test
  fun `test case 6 - project data gets into the system, the project ended more than 2 weeks ago and there is no typeform data`() {
    // given
    val projectId = "test case 6"
    val now = now()
    val start = now.minusSeconds(20) // one day before
    val end = now.minusSeconds(15)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      alertSender.sendProcessAlert("Submitted too late", any(), Scenario.RETRO)
    }
  }

  @Test
  fun `test case 7 - project data gets into the system more than a week before it starts, typeform surveys 1 week after project start`() {
    // given
    val projectId = "test case 7"
    val now = now()
    val start = now.plusSeconds(8)
    val end = start.plusSeconds(15)
    numberOfFilledSurveys(42)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_PRE_POST, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_1_T0, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.INFOMAIL_T1, Scenario.PRE_POST, any())
    }
  }

  @Test
  fun `test case 7a - project data gets into the system more than a week before it starts, no typeform surveys 1 week after project start`() {
    // given
    val projectId = "test case 7"
    val now = now()
    val start = now.plusSeconds(8)
    val end = start.plusSeconds(15)
    numberOfFilledSurveys(42)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_PRE_POST, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_1_T0, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_2_T0, Scenario.PRE_POST, any())
    }
  }

  @Test
  fun `test case 8 - project data gets into the system and there is a week or less until it starts`() {
    // given
    val projectId = "test case 8"
    val now = now()
    val start = now.plusSeconds(6)
    val end = start.plusSeconds(15)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_1_RETRO, Scenario.PRE_POST, any())
    }
  }

  @Test
  fun `test case 9 - project data gets into the system, the project has a duration of exactly 14 days, already started but at maximum one week ago and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 9"
    val now = now()
    val end = now.minusSeconds(6)
    val start = end.minusSeconds(14)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_1_RETRO, Scenario.PRE_POST, any())
    }
  }

  @Test
  fun `test case 10 - project data gets into the system, the project has a duration of exactly 14 days, already started but ended at maximum one week ago and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 10"
    val now = now()
    val end = now.minusSeconds(8)
    val start = end.minusSeconds(14)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_2_RETRO, Scenario.PRE_POST, any())
    }
  }

  @Test
  fun `test case 11 - project data gets into the system, the project has a duration of exactly 14 days, already started and ended at least two weeks ago and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 11"
    val now = now()
    val end = now.minusSeconds(15)
    val start = end.minusSeconds(14)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      alertSender.sendProcessAlert("What should be the message?", any(), Scenario.RETRO)
    }
  }

  @Test
  fun `test case 12 - project data gets into the system, the project has a duration of over 14 days, already started but not longer than one week ago and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 12"
    val now = now()
    val start = now.minusSeconds(5)
    val end = now.plusSeconds(10)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_PRE_POST, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_1_T0, Scenario.PRE_POST, any())
      alertSender.sendProcessAlert("No surveys received 2 weeks after project ended", any(), Scenario.RETRO)
    }
  }

  @Test
  fun `test case 13 - project data gets into the system, the project has a duration of over 14 days, already started more than one week ago but still has a week to go until it ends and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 13"
    val now = now()
    val start = now.minusSeconds(10)
    val end = now.plusSeconds(6)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_PRE_POST, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_2_T0, Scenario.PRE_POST, any())
      alertSender.sendProcessAlert("No surveys received 2 weeks after project ended", any(), Scenario.RETRO)
    }
  }

  @Test
  fun `test case 14 - project data gets into the system, the project has a duration of over 14 days, already started more than one week ago but still has a week to go until it ends and typeform data has been entered `() {
    // given
    val projectId = "test case 14"
    val now = now()
    val start = now.minusSeconds(10)
    val end = now.plusSeconds(6)
    numberOfFilledSurveys(42)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA // one day before GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_T1, Scenario.PRE_POST, any())
    }
  }

  @Test
  fun `test case 15 - project data gets into the system, the project has a duration of over 14 days, has less than one week until it ends and no typeform data has been entered yet`() {
    // given
    val projectId = "test case 15"
    val now = now()
    val end = now.plusSeconds(5)
    val start = end.minusSeconds(15)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_1_RETRO, Scenario.PRE_POST, any())
    }
  }

  @Test
  fun `test case 16 - project data gets into the system, the project has a duration of over 14 days, has ended not more than one week ago and no t1 typeform data has been entered yet`() {
    // given
    val projectId = "test case 16"
    val now = now()
    val end = now.minusSeconds(5)
    val start = end.minusSeconds(15)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      surveyEmailSender.send(MailType.INFOMAIL_RETRO, Scenario.PRE_POST, any())
      surveyEmailSender.send(MailType.REMINDER_2_RETRO, Scenario.PRE_POST, any())
    }
  }

  @Test
  fun `test case 17 - project data gets into the system, the project has a duration of over 14 days and ended more than two weeks ago and no t1 typeform data has been entered yet`() {
    // given
    val projectId = "test case 17"
    val now = now()
    val end = now.minusSeconds(15)
    val start = end.minusSeconds(15)
    numberOfFilledSurveys(0)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    verifySequence {
      alertSender.sendProcessAlert("What should be the message?", any(), Scenario.RETRO)
    }
  }

}
