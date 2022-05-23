/*
 * Copyright (c) 2021-2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.engine

import de.dkjs.survey.defineSurveyProcess
import de.dkjs.survey.mail.MailType
import de.dkjs.survey.mail.SurveyEmailSender
import de.dkjs.survey.model.*
import de.dkjs.survey.time.DkjsScheduler
import de.dkjs.survey.time.TimeConstraints
import de.dkjs.survey.time.TimeConstraintsFactory
import de.dkjs.survey.typeform.response.TypeformResponseChecker
import org.slf4j.Logger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime
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
  val environment: String,

)

fun defineProcess(block: DkjsSurveyProcessEngine.ProcessContext.() -> Unit):
    DkjsSurveyProcessEngine.ProcessContext.() -> Unit = block


@Singleton
@Component
class DkjsSurveyProcessEngine @Inject constructor(
  private val logger: Logger,
  private val projectRepository: ProjectRepository,
  private val processRepository: SurveyProcessRepository,
  private val activityRepository: ActivityRepository,
  private val surveyEmailSender: SurveyEmailSender,
  private val dkjsScheduler: DkjsScheduler,
  private val typeformChecker: TypeformResponseChecker,
  private val timeConstraintsFactory: TimeConstraintsFactory,
  private val alertSender: AlertSender
) {

  inner class ProcessContext(
    private val projectId: String,
    internal val projectStart: LocalDateTime, // TODO remove if never used
    internal val processStart: LocalDateTime,
    internal val time: TimeConstraints
  ) {

    fun execute(activity: String, block: ActivityContext.() -> String) {
      executeIfNotExecuted(activity) {
        dkjsScheduler.executeNow {
          executeActivity(activity, block)
        }
      }
    }

    // TODO should be removed if turns unnecessary
    fun scheduleCheck(
      activity: String,
      time: LocalDateTime,
      check: ActivityContext.() -> Boolean,
      onTrue: () -> Unit,
      onFalse: () -> Unit) {

      val process = processRepository.findByIdOrNull(projectId)!!
      val maybeChecked = process.activities.find { it.name == activity }
      if (maybeChecked == null) {
        dkjsScheduler.schedule(time) {
          executeActivity(activity) {
            val result = check()
            if (result) {
              onTrue()
            } else {
              onFalse()
            }
            result.toString()
          }
        }
      } else {
        val result = maybeChecked.result.toBoolean()
        if (result) {
          onTrue()
        } else {
          onFalse()
        }
      }
    }

    fun schedule(activity: String, time: LocalDateTime, block: ActivityContext.() -> String) {
      executeIfNotExecuted(activity) {
        logger.info("Process[${projectId}]: Scheduling activity '$activity' at $time")
        dkjsScheduler.schedule(time) {
          executeActivity(activity, block)
        }
      }
    }

    private fun executeIfNotExecuted(activity: String, block: () -> Unit) {
      val process = processRepository.findByIdOrNull(projectId)!!
      if (!process.alreadyExecuted(activity)) {
        block()
      }
    }

    private fun executeActivity(name: String, block: ActivityContext.() -> String) {
      logger.info("Process[$projectId]: Executing activity: '$name'")
      val project = projectRepository.findByIdOrNull(projectId)!!
      val process = project.surveyProcess!!
      val activity = try {
        val result = block(ActivityContext(project))
        logger.info(
          "Process[$projectId]: Activity successfully executed: '$name'"
        )
        Activity(
          surveyProcessId = process.id,
          name = name,
          result = result
        )
      } catch (e: Exception) {
        logger.error("Process[$projectId]: Activity failed: '$name'", e)
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
        alertSender.sendProcessAlert("Survey process failed", project)
        process.phase = SurveyProcess.Phase.FAILED
      }
      project.surveyProcess = processRepository.save(process)
      projectRepository.save(project)
    }

  }

  inner class ActivityContext(
    private val project: Project
  ) {

    fun send(mailType: MailType, surveyType: SurveyType): String {
      logger.info(
        "Process[${project.id}]: Sending ${mailType.name} mail"
      )
      surveyEmailSender.send(project, mailType, surveyType)
      return "Sent mail: ${mailType.name}"
    }

    fun hasSurveyResponses(surveyType: SurveyType): Boolean {
      logger.info(
        "Process[${project.id}]: Checking typeform answers"
      )
      val count = typeformChecker.countSurveys(project, surveyType)
      return (count > 0).apply {
        if (this) {
          logger.info(
            "Process[${project.id}]: typeform survey responses received: $count"
          )
        } else {
          logger.warn(
            "Process[${project.id}]: no typeform surveys responses received"
          )
        }
      }
    }

    fun hasNoSurveyResponses(surveyType: SurveyType): Boolean = ! hasSurveyResponses(surveyType)

    fun sendAlert(message: String): String {
      logger.info("Process[${project.id}]: Sending alert: $message")
      alertSender.sendProcessAlert(message, project)
      return "Alert sent: $message"
    }

    fun finishProcess(): String {
      logger.info("Process[${project.id}]: Finished")
      val process = project.surveyProcess!!
      process.phase = SurveyProcess.Phase.FINISHED
      processRepository.save(process)
      return "Process finished"
    }

  }

  val processDefinition = defineSurveyProcess()

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
  }

  fun handleProjects(projects: List<Project>) {
    logger.info("Handling project batch")
    // we need to persist all the projects asap
    val savedProjects = projects.map { project ->
      val process = processRepository.save(SurveyProcess(
        id = project.id,
        phase = SurveyProcess.Phase.ACTIVE
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
    logger.info("Process[${project.id}]: Starting")
    val time = timeConstraintsFactory.newTimeConstraints(project)
    val ctx = ProcessContext(
      projectId = project.id,
      projectStart = project.start,
      processStart = project.surveyProcess!!.start,
      time = time
    )
    processDefinition(ctx)
  }

}
