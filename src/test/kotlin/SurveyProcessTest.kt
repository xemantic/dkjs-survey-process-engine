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
import org.junit.jupiter.api.Test

@DkjsSurveyProcessEngineTest
class SurveyProcessTest : SurveyProcessTestBase() {

  @Test
  fun `test case 1 - project shorter than 14 days, project data gets into the system before the project starts, no data recorded during and after project`() {
    // given
    val projectId = "test case 1"
    val start = now() + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 13.days
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
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)     // conditional on typeform check SurveyType.POST
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 1a - project shorter than 14 days, project data gets into the system before the project starts, data recorded during or after project`() {
    // given
    val projectId = "test case 2"
    val start = now() + 1.days // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 13.days
    numberOfSurveyResponses(SurveyType.POST, 3)

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
  fun `test case 2 - project shorter than 14 days, the project has already started, but it didnt end yet, no answers are being recorded`() {
    // given
    val projectId = "test case 2a"
    val start = now() - 1.days 
    val end = start + 12.days
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
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)     // conditional on typeform check SurveyType.POST
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 2a - project shorter than 14 days, the project has already started, but it didnt end yet, data is being recorded`() {
    // given
    val projectId = "test case 2a"
    val start = now() - 1.days 
    val end = start + 12.days
    numberOfSurveyResponses(SurveyType.POST, 3)

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
  fun `test case 3 - project shorter than 14 days, the project has already finished, no data recorded after the project`() {
    // given
    val projectId = "test case 4"
    val end = now() - 6.days  // this schuld read: schedule the sequence below if the project ended less than 7 days ago, e.g. 6 or 5 or 4 days ago
    val start = end - 13.days
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
  fun `test case 3a - project shorter than 14 days, the project has already finsihed, data recorded after the project`() {
    // given
    val projectId = "test case 4"
    val end = now() - 6.days  // this schuld read: schedule the sequence below if the project ended less than 7 days ago, e.g. 6 or 5 or 4 days ago
    val start = end - 13.days
    numberOfSurveyResponses(SurveyType.POST, 3)


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
  fun `test case 4 - project duration is exactly 14 days, project data gets into the system more than a week before it starts, only pre data but no post data is being entered`() {
    // given
    val projectId = "test case 4"
    val start = now() + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 14.days
    numberOfSurveyResponses(SurveyType.PRE, 3)
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
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1, SurveyType.POST)      // conditional on typeform check SurveyType.POST
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

   @Test
  fun `test case 4a - project duration is exactly 14 days, project data gets into the system more than a week before it starts, no data is being entered`() {
    // given
    val projectId = "test case 4a"
    val start = now() + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 14.days
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
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1_RETRO, SurveyType.POST) //the mailtext is specifically for switching into the retro track conditional on no t0 data for exactly 14 day projects
      surveyEmailSender.send(any(), MailType.REMINDER_2_T1_RETRO, SurveyType.POST) // conditional on typeform check SurveyType.POST
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 4b - project duration is exactly 14 days, project data gets into the system more than a week before it starts, all data is being entered`() {
    // given
    val projectId = "test case 4b"
    val start = now() + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 14.days
    numberOfSurveyResponses(SurveyType.PRE, 3)
    numberOfSurveyResponses(SurveyType.POST, 3)

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

  @Test
  fun `test case 4c - project duration is exactly 14 days, project data gets into the system more than a week before it starts, only t1 data is being entered`() {
    // given
    val projectId = "test case 4c"
    val start = now() + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 14.days
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 3)

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
      surveyEmailSender.send(any(), MailType.REMINDER_2_T0, SurveyType.PRE)         // conditional on typeform check SurveyType.PRE
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1_RETRO, SurveyType.POST)  //the mailtext is specifically for switching into the retro track conditional on no t0 data for exactly 14 day projects
    }
  }

  @Test
  fun `test case 5 - project duration is over 14 days (eg 15,16,17, so on), and there is a week or less until it starts and only typeform data for t0 is recorded`() {
    // given
    val projectId = "test case 5"
    val start = now() + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 15.days     // this should read: the project ends 15 days or more after the project start
    numberOfSurveyResponses(SurveyType.PRE, 3)
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
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1, SurveyType.POST)        // conditional on typeform check SurveyType.POST
      surveyEmailSender.send(any(), MailType.REMINDER_2_T1_RETRO, SurveyType.POST)  // conditional on typeform check SurveyType.POST    
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }
  
  @Test
  fun `test case 5a - project duration is over 14 days (eg 15,16,17, so on), and there is a week or less until it starts and only typeform data for t1`() {
    // given
    val projectId = "test case 5a"
    val start = now() + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 15.days     // this should read: the project ends 15 days or more after the project start (e.g. 15 or 16 or 17 days etc.)
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 3)

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
      surveyEmailSender.send(any(), MailType.REMINDER_2_T0, SurveyType.PRE)      // conditional on typeform check SurveyType.PRE
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1, SurveyType.POST)     // conditional on typeform check SurveyType.POST
    }
  }
  
  @Test
  fun `test case 5b - project duration is over 14 days (eg 15,16,17, so on), and there is a week or less until it starts and data is being recorded for t0 and t1`() {
    // given
    val projectId = "test case 5b"
    val start = now() + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 15.days     // this should read: the project ends 15 days or more after the project start (e.g. 15 or 16 or 17 days etc.)
    numberOfSurveyResponses(SurveyType.PRE, 3)
    numberOfSurveyResponses(SurveyType.POST, 3)

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
  
  @Test
  fun `test case 5c - project duration is over 14 days (eg 15,16,17, so on), and there is a week or less until it starts and no data is recorded`() {
    // given
    val projectId = "test case 5c"
    val start = now() + 1.days    // this should read: schedule the sequence below for a project starting in 1 day or in 2 days or in 3 days etc. (1 day or more in the future)
    val end = start + 15.days     // this should read: the project ends 15 days or more after the project start (e.g. 15 or 16 or 17 days etc.)
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
      surveyEmailSender.send(any(), MailType.REMINDER_2_T0, SurveyType.PRE)           // conditional on typeform check SurveyType.PRE
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1, SurveyType.POST)          // conditional on typeform check SurveyType.POST 
      surveyEmailSender.send(any(), MailType.REMINDER_2_T1_RETRO, SurveyType.POST)    // conditional on typeform check SurveyType.POST    
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }
  
  
  @Test
  fun `test case 6 - project data gets into the system, the project has a duration of at least 7 weeks, already started but at maximum one week ago and no typeform data is recorded`() {
    // given
    val projectId = "test case 6"
    val start = now() - 7.days    // this should read: the project started 7 days ago or less (e.g. 7 days ago or 6 days ago or 5 days ago...)
    val end = now() + 42.days     // this should read: the project ends in 42 days or more (e.g. in 42 days or 43 days or 44 days...)
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
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T0, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1_RETRO, SurveyType.POST)    // conditional on typeform check SurveyType.POST 
      surveyEmailSender.send(any(), MailType.REMINDER_2_T1_RETRO, SurveyType.POST)    // conditional on typeform check SurveyType.POST 
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 6a - project data gets into the system, the project has a duration of at least 7 weeks, already started but at maximum one week ago and no typeform data t0 but t1`() {
    // given
    val projectId = "test case 6a"
    val start = now() - 7.days    // this should read: the project started 7 days ago or less (e.g. 7 days ago or 6 days ago or 5 days ago...)
    val end = now() + 42.days     // this should read: the project ends in 42 days or more (e.g. in 42 days or 43 days or 44 days...)
    numberOfSurveyResponses(SurveyType.PRE, 0)
    numberOfSurveyResponses(SurveyType.POST, 3)


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
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T0, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1_RETRO, SurveyType.POST)  // conditional on typeform check SurveyType.POST 
    }
  }

   @Test
  fun `test case 6b - project data gets into the system, the project has a duration of at least 7 weeks, already started but at maximum one week ago and no typeform data is recorded at t1 but at t0`() {
    // given
    val projectId = "test case 6b"
    val start = now() - 7.days    // this should read: the project started 7 days ago or less (e.g. 7 days ago or 6 days ago or 5 days ago...)
    val end = now() + 42.days     // this should read: the project ends in 42 days or more (e.g. in 42 days or 43 days or 44 days...)
    numberOfSurveyResponses(SurveyType.PRE, 3)
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
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T0, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.POST)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T1, SurveyType.POST)      // conditional on typeform check SurveyType.POST 
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }
 
  @Test
  fun `test case 6c - project data gets into the system, the project has a duration of at least 7 weeks, already started but at maximum one week ago and data is recorded`() {
    // given
    val projectId = "test case 6c"
    val start = now() - 7.days    // this should read: the project started 7 days ago or less (e.g. 7 days ago or 6 days ago or 5 days ago...)
    val end = now() + 42.days     // this should read: the project ends in 42 days or more (e.g. in 42 days or 43 days or 44 days...)
    numberOfSurveyResponses(SurveyType.PRE, 3)
    numberOfSurveyResponses(SurveyType.POST, 3)


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
      surveyEmailSender.send(any(), MailType.INFOMAIL_PRE_POST, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.REMINDER_1_T0, SurveyType.PRE)
      surveyEmailSender.send(any(), MailType.INFOMAIL_T1, SurveyType.POST)
    }
  } 
 
  @Test
  fun `test case 6,5 - project data gets into the system, the project has a duration of at least 7 weeks, already started but at maximum one week ago and no typeform data is recorded`() {
    // given
    val projectId = "test case 6.5"
    val start = now() - 7.days    // this should read: the project started 7 days ago or less (e.g. 7 days ago or 6 days ago or 5 days ago...)
    val end = start + 15.days     
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
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)     // conditional on typeform check SurveyType.POST
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }

  @Test
  fun `test case 6,5a - project data gets into the system, the project has a duration of at least 7 weeks, already started but at maximum one week ago and no typeform data t0 but t1`() {
    // given
    val projectId = "test case 6.5a"
    val start = now() - 7.days    // this should read: the project started 7 days ago or less (e.g. 7 days ago or 6 days ago or 5 days ago...)
    val end = start + 15.days
    numberOfSurveyResponses(SurveyType.POST, 3)


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
    }
  }

  @Test
  fun `test case 7 - project data gets into the system, the project has a duration of at least 7 weeks, already started but more than one week ago and no typeform data is recorded`() {
    // given
    val projectId = "test case 7"
    val start = now() - 8.days    // this should read: the project started 8 days ago or more (e.g. 8 days ago or 9 days ago or 10 days ago)
    val end = now() + 41.days     // this should read: the project ends in 41 days or more (e.g. in 42 days or 43 days or 44 days... corresponding to val start)
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
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)   // conditional on typeform check SurveyType.POST 
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }
 
 
  @Test
  fun `test case 7a - project data gets into the system, the project has a duration of at least 7 weeks, already started but more than one week ago and typeform data is recorded`() {
    // given
    val projectId = "test case 7a"
    val start = now() - 8.days    // this should read: the project started 8 days ago or more (e.g. 8 days ago or 9 days ago or 10 days ago)
    val end = now() + 41.days     // this should read: the project ends in 41 days or more (e.g. in 42 days or 43 days or 44 days... corresponding to val start)
    numberOfSurveyResponses(SurveyType.POST, 3)


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
    }
  }
 
 
  @Test
  fun `test case 8 - project data gets into the system, the project has a duration of exactly 14 days, already started and no typeform data is recorded`() {
    // given
    val projectId = "test case 8"
    val start = now() - 1.days      // this should read: the project start one day ago or longer (e.g. 1 day ago or 2 days ago or 3 days ago...)
    val end = now() + 13.days       // this should read: the project ends on its 14th day
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
      surveyEmailSender.send(any(), MailType.REMINDER_2_RETRO, SurveyType.POST)     // conditional on typeform check SurveyType.POST 
      alertSender.sendProcessAlert("No survey responses received 2 weeks after project ended", any())
    }
  }
 
 
  @Test
  fun `test case 8 - project data gets into the system, the project has a duration of exactly 14 days, already started and typeform data is recorded`() {
    // given
    val projectId = "test case 8a"
    val start = now() - 1.days      // this should read: the project start one day ago or longer (e.g. 1 day ago or 2 days ago or 3 days ago...)
    val end = now() + 13.days       // this should read: the project ends on its 14th day
    numberOfSurveyResponses(SurveyType.POST, 3)

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
  fun `test case 9 - project data gets into the system, regardless of project duration, ended one week ago and no typeform data is recorded`() {
    // given
    val projectId = "test case 9"
    val end = now() - 6.days  // this should read: all projects that ended 6 days ago or less, regardless of project start and duration
    val start = end - 14.days // this should read: the project starts 14 days or more before its end
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
  fun `test case 9a - project data gets into the system, regardless of project duration, ended one week ago and typeform data is recorded`() {
    // given
    val projectId = "test case 9a"
    val end = now() - 6.days    // this should read: all projects that ended 6 days ago or less, regardless of project start and duration
    val start = end - 14.days   // this should read: the project starts 14 days or more before its end
    numberOfSurveyResponses(SurveyType.POST, 3)

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
  fun `test case 10 - project data gets into the system, regardless of project duration, ended one week ago or more and typeform data is recorded`() {
    // given
    val projectId = "test case 10"
    val end = now() - 7.days    // this should read: all projects that ended 7 days ago or more, regardless of project start and duration
    val start = end             // this should read: the project starts on the same day it ends
    numberOfSurveyResponses(SurveyType.POST, 3)

    // when
    uploadingProjectsCsv("""
      "project.number";"project.status";"project.provider";"provider.number";"project.pronoun";"project.firstname";"project.lastname";"project.mail";"project.name";"participants.age1to5";"participants.age6to10";"participants.age11to15";"participants.age16to19";"participants.age20to26";"participants.worker";"project.goals";"project.start";"project.end"
      "$projectId";"50 - bewilligt";"serious; business ÖA GmbH";123456;"Frau";"Maxi";"Musterfräulein";"p1urtümlich@example.com";"Make ducks cuter";0;0;250;50;0;NA;"01,05,03";"${start.dkjsDateTime}";"${end.dkjsDateTime}"
    """)
    waitingUntilProcessEnds(projectId)

    // then
    // @Kazik, is it possible to create something like this? For every project that ended more than two weeks ago, don't send an email with alertSender.sendProcessAlert, but register the 'error' of abscence of data in the database
  }

}
