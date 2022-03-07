/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.mail.SurveyEmailSender
import de.dkjs.survey.mail.MailType
import de.dkjs.survey.model.*
import de.dkjs.survey.typeform.link.TypeformSurveyLinkGenerator
import de.dkjs.survey.typeform.response.TypeformResponseChecker
import org.slf4j.Logger
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component
class DkjsSurveyProcessEngine @Inject constructor(
  private val logger: Logger,
  private val repository: ProjectRepository,
  private val linkGenerator: TypeformSurveyLinkGenerator,
  private val emailService: SurveyEmailSender,
  private val taskScheduler: TaskScheduler,
  private val typeformChecker: TypeformResponseChecker
) {

  @PostConstruct
  fun start() {
    logger.info("Starting DkjsSurveyProcessEngine")
    val finishedCount = repository.countBySurveyProcessPhase(SurveyProcess.Phase.FINISHED)
    val unfinished = repository.findBySurveyProcessPhaseNot(SurveyProcess.Phase.FINISHED)
    val unfinishedCount = unfinished.size
    logger.info("  - finished processes: $finishedCount")
    logger.info("  - unfinished processes: $unfinishedCount")
    unfinished.forEach {
      handle(it)
    }
    logger.info("DkjsSurveyProcessEngine started")
  }

  fun projectExists(projectNumber: String) = repository.existsById(projectNumber)

  fun handleNew(project: Project) {
    logger.info("Starting process for project: ${project.id}")
    logger.info("TODO")
    // TODO: create a surveyProcess if it is null
    //project.surveyProcess.phase = SurveyProcess.Phase.PERSISTED
    //handle(repository.save(project))
  }

  private fun handle(project: Project) {

    logger.debug("Handling project: ${project.id}")

    fun send(mailType: MailType, scenarioType: ScenarioType) = send(mailType, project, scenarioType)
    fun scheduleAt(date: LocalDateTime, call: () -> Unit) = scheduleAt(project, date, call)
    fun hasNoAnswers() = typeformChecker.countSurveys("foo", project.id) == 0 // TODO we need form selecting logic here

    val now = LocalDateTime.now()

    val projectDurationDays = Duration.between(project.start, project.end).toDays()
    // TODO is it really a good condition to trigger short scenario?
    val shortScenario = projectDurationDays <= 13

    val scenarioType = if (shortScenario) ScenarioType.PRE else ScenarioType.POST
    if (shortScenario) {

      logger.debug("Duration longer than 2 weeks = short short scenario for project: ${project.id}")

      // TODO: create a surveyProcess if it is null
//      if (project.surveyProcess.notifications.isEmpty()) {
//        send(MailType.INFOMAIL_RETRO)
//      }

      scheduleAt(project.end.minusWeeks(2)) {
        send(MailType.REMINDER_1_RETRO, scenarioType)
        // TODO: create a surveyProcess if it is null
        // project.surveyProcess.phase = SurveyProcess.Phase.FINISHED
        repository.save(project)
      }

    } else {

      logger.debug("Duration shorter than 2 weeks = long scenario for project: ${project.id}")

      // TODO: project.surveyProcess can be null
//      if (project.surveyProcess.notifications.isEmpty()) {
//        send(MailType.INFOMAIL_PRE_POST)
//      }
//
//      if (!project.surveyProcess.notifications.any {
//        it.mailType in setOf(MailType.REMINDER_1_RETRO, MailType.REMINDER_1_T0)
//      }) {
//      }

      if (now.isAfter(project.start.plusWeeks(1))) {

        if (projectDurationDays < 14) {
          send(MailType.REMINDER_1_RETRO, scenarioType)
        } else {
          send(MailType.REMINDER_1_T0, scenarioType)
        }

        scheduleAt(project.start) { //TODO Julia - start date or end date?
          if (hasNoAnswers()) {
            send(MailType.REMINDER_2_T0, scenarioType)
          }
        }

        send(MailType.REMINDER_1_T0, scenarioType)

      }

      scheduleAt(project.end.minusWeeks(1)) {
        send(MailType.INFOMAIL_T1, scenarioType)
      }

      scheduleAt(project.end) {
        send(MailType.REMINDER_1_T1, scenarioType)
        if (hasNoAnswers()) {
          send(MailType.REMINDER_2_RETRO, scenarioType)
        }
        // TODO when is it finalized?
      }

    }
  }

  private fun send(
    mailType: MailType,
    project: Project,
    scenarioType: ScenarioType
  ) {
    logger.debug("Sending ${mailType.name} mail to project: ${project.id} in scenario ${scenarioType.name}")
    emailService.send(mailType, project, scenarioType)
    // TODO: init project.surveyProcess
//    project.surveyProcess.notifications.add(Notification(
//      id = 0,
//      mailType = mailType
//    ))
    repository.save(project)
  }

  private fun scheduleAt(project: Project, date: LocalDateTime, call: () -> Unit) {
    logger.debug("Scheduling action at $date for project: ${project.id}")
    taskScheduler.schedule(
      {
        logger.debug("Running scheduled action for project: ${project.id}")
        call()
      },
      // TODO is it correct?
      date.toInstant(ZoneOffset.UTC)
    )
  }

}
