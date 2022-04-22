/*
 * Copyright (c) 2021-2022 Kazimierz Pogoda / Xemantic
 */

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
import org.springframework.data.repository.findByIdOrNull
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
import javax.validation.constraints.NotNull

@Validated
@ConfigurationProperties("dkjs")
@ConstructorBinding
data class DkjsConfig(

  /**
   * Might be `DEV`, `TEST`, `CI`, `PROD`, an arbitrary string used in the logs and alert emails
   * to distinguish environments.
   */
  @get:NotEmpty
  val environment: String,

)

@Profile("test")
@Validated
@ConfigurationProperties("test")
@ConstructorBinding
data class TestConfig(

  /**
   * Test [TimeConstraints] are operating on second basis instead of day/week basis
   * for time calculation.
   */
  @get:NotNull
  val dayDurationAsNumberOfSeconds: Int

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
class TestTimeConstraintsFactory @Inject constructor(
  private val config: TestConfig
) : TimeConstraintsFactory {
  override fun newTimeConstraints(project: Project) = TestTimeConstraints(project, config)
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

class TestTimeConstraints(
  private val project: Project,
  testConfig: TestConfig
) : TimeConstraints {
  private val multiplier: Long = testConfig.dayDurationAsNumberOfSeconds.toLong()
  override val scenario: Scenario get() =
    if (Duration.between(project.start, project.end).toSeconds() <= 13 * multiplier) Scenario.RETRO
    else Scenario.PRE_POST
  override val is14DaysProjectDuration: Boolean get() = Duration.between(project.start, project.end).toSeconds() == (14 * multiplier)
  override val oneWeekBeforeProjectStarts: LocalDateTime get() = project.start.minusSeconds(7 * multiplier)
  override val oneWeekAfterProjectStarts: LocalDateTime get() = project.start.plusSeconds(7 * multiplier)
  override val twoWeeksAfterProjectStarts: LocalDateTime get() = project.start.plusSeconds(14 * multiplier)
  override val oneWeekBeforeProjectEnds: LocalDateTime get() = project.end.minusSeconds(7 * multiplier)
  override val oneWeekAfterProjectEnds: LocalDateTime get() = project.end.plusSeconds(7 * multiplier)
  override val twoWeeksAfterProjectEnds: LocalDateTime get() = project.end.plusSeconds(14 * multiplier)
}

@Singleton
@Component
class DkjsSurveyProcessEngine @Inject constructor(
  private val logger: Logger,
  private val projectRepository: ProjectRepository,
  private val processRepository: SurveyProcessRepository,
  private val activityRepository: ActivityRepository,
  private val emailService: SurveyEmailSender,
  private val taskScheduler: TaskScheduler,
  private val taskExecutor: SchedulingTaskExecutor,
  private val typeformChecker: TypeformResponseChecker,
  private val timeConstraintsFactory: TimeConstraintsFactory,
  private val alertEmailSender: AlertEmailSender
) {

  inner class ProcessContext(
    private val projectId: String,
    internal val time: TimeConstraints
  ) {

    fun execute(activity: String, block: ActivityContext.() -> String) {
      executeIfNotExecuted(activity) {
        taskExecutor.submit {
          logger.info("Executing activity: '$activity' for project: $projectId")
          executeActivity(activity, block)
        }
      }
    }

    fun schedule(activity: String, time: LocalDateTime, block: ActivityContext.() -> String) {
      executeIfNotExecuted(activity) {
        logger.info("Scheduling activity '$activity' at $time for project: $projectId")
        taskScheduler.schedule(
          {
            logger.info("Running scheduled action for project: $projectId")
            executeActivity(activity, block)
          },
          // TODO is it correct?
          time.toInstant(ZoneOffset.UTC)
        )
      }
    }

    private fun executeIfNotExecuted(activity: String, block: () -> Unit) {
      val process = processRepository.findByIdOrNull(projectId)!!
      if (!process.alreadyExecuted(activity)) {
        block()
      }
    }

    private fun executeActivity(name: String, block: ActivityContext.() -> String) {
      val project = projectRepository.findByIdOrNull(projectId)!!
      val process = project.surveyProcess!!
      val activity = try {
        val result = block(ActivityContext(project, time.scenario))
        Activity(
          surveyProcessId = process.id,
          name = name,
          result = result
        )
      } catch (e: Exception) {
        Activity(
          surveyProcessId = process.id,
          name,
          failure = e.message
        )
      }
      process.activities.add(
        activityRepository.save(activity)
      )
      if (activity.failed) {
        sendAlert(project, time.scenario, "Survey process failed")
        process.phase = SurveyProcess.Phase.FAILED
      }
      project.surveyProcess = processRepository.save(process)
      projectRepository.save(project)
    }

  }

  inner class ActivityContext(
    private val project: Project,
    private val scenario: Scenario
  ) {

    fun send(mailType: MailType): String {
      logger.info(
        "Sending ${mailType.name} mail to project: ${project.id} " +
          "in scenario ${scenario.name}"
      )
      emailService.send(mailType, scenario, project)
      return "Sent mail: ${mailType.name}"
    }

    fun hasNoAnswers(): Boolean {
      logger.info(
        "Checking typeform answers for project: ${project.id}, scenario ${scenario.name}"
      )
      val count = typeformChecker.countSurveys(project, scenario)
      val noAnswers = if (count == 0) {
        logger.warn(
          "No typeform surveys filled for project: ${project.id}, scenario ${scenario.name}"
        )
        true
      } else {
        logger.info(
          "Typeform survey received for project: ${project.id}, scenario ${scenario.name}, count: $count"
        )
        false
      }
      return noAnswers
    }

    fun sendAlert(message: String): String {
      logger.info("Sending alert about project: ${project.id} - $message")
      sendAlert(project, scenario, message)
      return "Alert sent: $message"
    }

    fun finishProcess(): String {
      logger.info("Survey process has finished for project: ${project.id}")
      val process = project.surveyProcess!!
      process.phase = SurveyProcess.Phase.FINISHED
      processRepository.save(process)
      return "Process finished"
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
      val process = processRepository.save(SurveyProcess(
        project.id, SurveyProcess.Phase.ACTIVE
      ))
      project.surveyProcess = process
      projectRepository.save(project)
    }
    logger.info("Project batch persisted")
    savedProjects.forEach {
      startProcess(it)
    }
  }

  fun startProcess(project: Project) {
    logger.info("Starting process for project: ${project.id}")
    val time = timeConstraintsFactory.newTimeConstraints(project)
    val ctx = ProcessContext(project.id, time)
    processDefinitions[time.scenario]!!(ctx)
  }

  fun sendAlert(project: Project, scenario: Scenario, message: String) {
    val contact = project.contactPerson
    alertEmailSender.sendAlertEmail(
      "$message, project: ${project.id}",
      """
            $message

            Project details:
            - number: ${project.id}
            - name: ${project.name}
            - status: ${project.status}
            - scenario: $scenario
            - goals: ${project.goals}
            - start: ${project.start}
            - end: ${project.end}
            - contact: ${contact.pronoun} ${contact.firstName} ${contact.lastName}
            - email: ${contact.email}
            - provider number: ${project.provider.id}
            - provider name: ${project.provider.id}
          """.trimIndent()
    )
  }

}
