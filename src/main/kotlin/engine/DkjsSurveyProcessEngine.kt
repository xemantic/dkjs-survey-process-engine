/*
 * Copyright (c) 2021-2022 Kazimierz Pogoda / Xemantic
 */

// TODO review again at the end
package de.dkjs.survey.engine

import de.dkjs.survey.definePrePostProcess
import de.dkjs.survey.defineRetroProcess
import de.dkjs.survey.mail.AlertEmailSender
import de.dkjs.survey.mail.MailType
import de.dkjs.survey.mail.SurveyEmailSender
import de.dkjs.survey.model.*
import de.dkjs.survey.typeform.response.TypeformResponseChecker
import org.slf4j.Logger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Profile
import org.springframework.mail.MailException
import org.springframework.scheduling.SchedulingTaskExecutor
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotEmpty

@Validated
@ConfigurationProperties("dkjs")
@ConstructorBinding
data class DkjsConfig(

  /**
   * Might be `DEV`, `TEST`, `CI`, `PROD`, an arbitrary string used in the logs and alert emails
   * to distinguish environments.
   */
  @get:NotEmpty
  val environment: String

)

interface TimeConstraints {
  val scenario: Scenario
  val is14DaysProjectDuration: Boolean
  val oneWeekBeforeProjectStarts: LocalDateTime
  val oneWeekAfterProjectStarts: LocalDateTime
  val twoWeeksAfterProjectStarts: LocalDateTime
  val oneWeekBeforeProjectEnds: LocalDateTime
  val oneWeekAfterProjectEnds: LocalDateTime
  val twoWeeksAfterProjectEnds: LocalDateTime
}

fun defineProcess(block: DkjsSurveyProcessEngine.ProcessContext.() -> Unit):
    DkjsSurveyProcessEngine.ProcessContext.() -> Unit = block

interface TimeConstraintsFactory {
  fun newTimeConstraints(project: Project): TimeConstraints
}

@Component
@Profile("prod")
class ProductionTimeConstraintsFactory : TimeConstraintsFactory {
  override fun newTimeConstraints(project: Project) = ProductionTimeConstraints(project)
}

@Component
@Profile("test")
class TestTimeConstraintsFactory : TimeConstraintsFactory {
  override fun newTimeConstraints(project: Project) = TestTimeConstraints(project)
}

class ProductionTimeConstraints(private val project: Project) : TimeConstraints {
  override val scenario: Scenario get() =
    if (Duration.between(project.start, project.end).toDays() <= 13) Scenario.RETRO
    else Scenario.PRE_POST
  override val is14DaysProjectDuration: Boolean get() = Duration.between(project.start, project.end).toDays() == 14L
  override val oneWeekBeforeProjectStarts: LocalDateTime get() = project.start.minusWeeks(1)
  override val oneWeekAfterProjectStarts: LocalDateTime get() = project.start.plusWeeks(1)
  override val twoWeeksAfterProjectStarts: LocalDateTime get() = project.start.plusWeeks(2)
  override val oneWeekBeforeProjectEnds: LocalDateTime get() = project.end.minusWeeks(1)
  override val oneWeekAfterProjectEnds: LocalDateTime get() = project.end.plusWeeks(1)
  override val twoWeeksAfterProjectEnds: LocalDateTime get() = project.end.plusWeeks(2)
}

class TestTimeConstraints(private val project: Project) : TimeConstraints {
  override val scenario: Scenario get() =
    if (Duration.between(project.start, project.end).toSeconds() <= 13) Scenario.RETRO
    else Scenario.PRE_POST
  override val is14DaysProjectDuration: Boolean get() = Duration.between(project.start, project.end).toSeconds() == 14L
  override val oneWeekBeforeProjectStarts: LocalDateTime get() = project.start.minusSeconds(1 * 7)
  override val oneWeekAfterProjectStarts: LocalDateTime get() = project.start.plusSeconds(1 * 7)
  override val twoWeeksAfterProjectStarts: LocalDateTime get() = project.start.plusSeconds(2 * 7)
  override val oneWeekBeforeProjectEnds: LocalDateTime get() = project.end.minusSeconds(1 * 7)
  override val oneWeekAfterProjectEnds: LocalDateTime get() = project.end.plusSeconds(1 * 7)
  override val twoWeeksAfterProjectEnds: LocalDateTime get() = project.end.plusSeconds(2 * 7)
}

@Singleton
@Component
class DkjsSurveyProcessEngine @Inject constructor(
  private val logger: Logger,
  private val projectRepository: ProjectRepository,
  private val processRepository: SurveyProcessRepository,
  private val emailService: SurveyEmailSender,
  private val taskScheduler: TaskScheduler,
  private val taskExecutor: SchedulingTaskExecutor,
  private val typeformChecker: TypeformResponseChecker,
  private val timeConstraintsFactory: TimeConstraintsFactory,
  private val alertEmailSender: AlertEmailSender
) {

  inner class ProcessContext(private val project: Project) {

    val time = timeConstraintsFactory.newTimeConstraints(project)

    private val scheduledContext = ScheduledContext(project, time.scenario)

    fun sendImmediately(mailType: MailType) {
      if (isNotSent(mailType)) {
        taskExecutor.submit {
          send(mailType)
        }
      }
    }

    fun scheduleMailAt(time: LocalDateTime, mailType: MailType) {
      if (isNotSent(mailType)) {
        scheduleAt(time, project) {
          send(mailType)
        }
      }
    }

    fun scheduleIfNotSent(
      time: LocalDateTime,
      mailType: Set<MailType>,
      block: ScheduledContext.() -> Unit
    ) {
      if (isNotSent(mailType)) {
        scheduleAt(time, project) {
          block.invoke(scheduledContext)
        }
      }
    }

    fun scheduleAt(time: LocalDateTime, block: ScheduledContext.() -> Unit) {
      scheduleAt(time, project) {
        block.invoke(scheduledContext)
      }
    }

    private fun isNotSent(mailType: MailType): Boolean = isNotSent(setOf(mailType))

    private fun isNotSent(mailTypes: Set<MailType>): Boolean =
      !project.surveyProcess!!.isAlreadySent(mailTypes)

    private fun send(mailType: MailType) = send(mailType, project, time.scenario)

  }

  inner class ScheduledContext(
    private val project: Project,
    private val scenario: Scenario
  ) {

    fun send(mailType: MailType) = send(mailType, project, scenario)

    fun hasNoAnswers() = typeformChecker.countSurveys(project, scenario) == 0

    fun sendAlert(message: String) {
      alertEmailSender.sendAlertEmail(message, message)
    }

    fun finishProcess() {
      val process = project.surveyProcess!!
      process.phase = SurveyProcess.Phase.FINISHED
      processRepository.save(process)
    }

  }

  private val processDefinitions = mapOf(
    Scenario.RETRO to defineRetroProcess(),
    Scenario.PRE_POST to definePrePostProcess()
  )

  @PostConstruct
  fun start() {
    logger.info("Starting DkjsSurveyProcessEngine")
    val finishedCount = projectRepository.countBySurveyProcessPhase(SurveyProcess.Phase.FINISHED)
    val failedCount = projectRepository.countBySurveyProcessPhase(SurveyProcess.Phase.FAILED)
    val activeProcesses = projectRepository.findBySurveyProcessPhase(SurveyProcess.Phase.ACTIVE)
    logger.info("Process summary:")
    logger.info("    active: ${activeProcesses.size}")
    logger.info("  finished: $finishedCount")
    logger.info("    failed: $failedCount")
    activeProcesses.forEach {
      startProcess(it)
    }
    logger.info("DkjsSurveyProcessEngine started")
    alertEmailSender.sendAlertEmail(
      "DkjsSurveyProcessEngine started",
      """
        Process summary:
        active: ${activeProcesses.size}
        finished: $finishedCount
        failed: $failedCount
      """.trimIndent()
    )
  }

  fun handleProjects(projects: List<Project>) {
    logger.info("Handling project batch")
    // we need to persist all the projects asap
    val savedProjects = projects.map { project ->
      project.surveyProcess = SurveyProcess(
        project.id, SurveyProcess.Phase.ACTIVE
      )
      projectRepository.save(project)
    }
    logger.info("Project batch persisted")
    savedProjects.forEach {
      startProcess(it)
    }
  }

  fun startProcess(project: Project) {
    logger.info("Starting process for project: ${project.id}")
    val ctx = ProcessContext(project)
    processDefinitions[ctx.time.scenario]!!.invoke(ctx)
  }

  private fun send(
    mailType: MailType,
    project: Project,
    scenario: Scenario
  ) {
    logger.info("Sending ${mailType.name} mail to project: ${project.id} in scenario ${scenario.name}")
    val process = project.surveyProcess!!
    val notification = try {
      emailService.send(mailType, scenario, project)
      Notification(mailType = mailType)
    } catch (e : MailException) {
      logger.error("Error sending ${mailType.name} mail to project: ${project.id} in scenario ${scenario.name}", e)
      process.phase = SurveyProcess.Phase.FAILED
      Notification(mailType = mailType, failure = e.message)
    }
    process.notifications.add(notification)
    processRepository.save(process)
    if (process.phase == SurveyProcess.Phase.FAILED) {
      taskExecutor.submit {
        alertEmailSender.sendAlertEmail(
          "Error sending mail to project: ${project.id}",
          "Error sending ${mailType.name} mail to project: ${project.id} in scenario ${scenario.name}"
        )
      }
    }
  }

  // TODO move the where the project is known
  private fun scheduleAt(date: LocalDateTime, project: Project, call: () -> Unit) {
    logger.info("Scheduling action at $date for project: ${project.id}")
    taskScheduler.schedule(
      {
        logger.info("Running scheduled action for project: ${project.id}")
        call()
      },
      // TODO is it correct?
      date.toInstant(ZoneOffset.UTC)
    )
  }

}

