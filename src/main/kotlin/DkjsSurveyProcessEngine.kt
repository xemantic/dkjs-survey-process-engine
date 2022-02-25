/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.mail.SurveyEmailSender
import de.dkjs.survey.mail.MailType
import de.dkjs.survey.mail.TypeformSurveyLinkGenerator
import de.dkjs.survey.model.*
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
  private val projectRepository: ProjectRepository,
  private val linkGenerator: TypeformSurveyLinkGenerator,
  private val emailService: SurveyEmailSender,
  private val taskScheduler: TaskScheduler,
  private val typeformChecker: TypeformResponseChecker
) {

  @PostConstruct
  fun start() {
    logger.info("Starting DkjsSurveyProcessEngine")
    val finishedCount = projectRepository.countBySurveyProcessPhase(SurveyProcess.Phase.FINISHED)
    val unfinished = projectRepository.findBySurveyProcessPhaseNot(SurveyProcess.Phase.FINISHED)
    val unfinishedCount = unfinished.size
    logger.info("  - finished processes: $finishedCount")
    logger.info("  - unfinished processes: $unfinishedCount")
    unfinished.forEach {
      handle(it)
    }
    logger.info("DkjsSurveyProcessEngine started")
  }

  fun projectExists(projectNumber: String) = projectRepository.existsById(projectNumber)

  fun handleNew(project: Project) {
    logger.info("Starting process for project: ${project.id}")
    // TODO: create a surveyProcess if it is null
    project.surveyProcess = SurveyProcess(project.id, SurveyProcess.Phase.PERSISTED)
    handle(projectRepository.save(project))
  }

  private fun handle(project: Project) {

    logger.debug("Handling project: ${project.id}")

    fun send(mailType: MailType) = send(project, mailType)
    fun scheduleAt(date: LocalDateTime, call: () -> Unit) = scheduleAt(project, date, call)
    fun hasNoAnswers() = typeformChecker.countSurveys(project.id) == 0

    val now = LocalDateTime.now()

    val projectDurationDays = Duration.between(project.start, project.end).toDays()
    // TODO is it really a good condition to trigger short scenario?
    val shortScenario = projectDurationDays <= 13

    if (shortScenario) {

      logger.debug("Duration longer than 2 weeks = short short scenario for project: ${project.id}")

      // TODO: create a surveyProcess if it is null
//      if (project.surveyProcess.notifications.isEmpty()) {
//        send(MailType.INFOMAIL_RETRO)
//      }

      scheduleAt(project.end.minusWeeks(2)) {
        send(MailType.REMINDER_1_RETRO)
        // TODO: create a surveyProcess if it is null
        // project.surveyProcess.phase = SurveyProcess.Phase.FINISHED
        projectRepository.save(project)
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
          send(MailType.REMINDER_1_RETRO)
        } else {
          send(MailType.REMINDER_1_T0)
        }

        scheduleAt(project.start) { //TODO Julia - start date or end date?
          if (hasNoAnswers()) {
            send(MailType.REMINDER_2_T0)
          }
        }

        send(MailType.REMINDER_1_T0)

      }

      scheduleAt(project.end.minusWeeks(1)) {
        send(MailType.INFOMAIL_T1)
      }

      scheduleAt(project.end) {
        send(MailType.REMINDER_1_T1)
        if (hasNoAnswers()) {
          send(MailType.REMINDER_2_RETRO)
        }
        // TODO when is it finalized?
      }

    }
  }

  private fun send(project: Project, mailType: MailType) {
    logger.debug("Sending ${mailType.name} mail to project: ${project.id}")
    emailService.send(mailType, project)
    // TODO: init project.surveyProcess
//    project.surveyProcess.notifications.add(Notification(
//      id = 0,
//      mailType = mailType
//    ))
    projectRepository.save(project)
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
