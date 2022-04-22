/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.time

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.Scenario
import org.springframework.context.annotation.Profile
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * A scheduler which takes configured time zone into account when
 * scheduling
 */
@Component
class DkjsScheduler @Inject constructor(
  config: TimeConfig,
  private val executor: TaskExecutor,
  private val scheduler: TaskScheduler
) {

  val zoneId = config.zoneId

  fun executeNow(block: () -> Unit) {
    executor.execute(block)
  }

  fun schedule(time: LocalDateTime, block: () -> Unit) {
    val offset = zoneId.rules.getOffset(time)
    val instant = time.toInstant(offset)
    scheduler.schedule(block, instant)
  }

}

interface TimeConstraints {
  val scenario: Scenario
  val isMoreThan14DaysProjectDuration: Boolean
  val oneWeekBeforeProjectStarts: LocalDateTime
  val oneWeekAfterProjectStarts: LocalDateTime
  val twoWeeksAfterProjectStarts: LocalDateTime
  val oneWeekBeforeProjectEnds: LocalDateTime
  val oneWeekAfterProjectEnds: LocalDateTime
  val twoWeeksAfterProjectEnds: LocalDateTime
}

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
  private val config: TimeConfig
) : TimeConstraintsFactory {
  override fun newTimeConstraints(project: Project) = TestTimeConstraints(project, config)
}

class ProductionTimeConstraints(private val project: Project) : TimeConstraints {
  override val scenario: Scenario get() =
    if (Duration.between(project.start, project.end).toDays() <= 13) Scenario.RETRO
    else Scenario.PRE_POST
  override val isMoreThan14DaysProjectDuration: Boolean get() = Duration.between(project.start, project.end).toDays() > 14L
  override val oneWeekBeforeProjectStarts: LocalDateTime get() = project.start.minusWeeks(1)
  override val oneWeekAfterProjectStarts: LocalDateTime get() = project.start.plusWeeks(1)
  override val twoWeeksAfterProjectStarts: LocalDateTime get() = project.start.plusWeeks(2)
  override val oneWeekBeforeProjectEnds: LocalDateTime get() = project.end.minusWeeks(1)
  override val oneWeekAfterProjectEnds: LocalDateTime get() = project.end.plusWeeks(1)
  override val twoWeeksAfterProjectEnds: LocalDateTime get() = project.end.plusWeeks(2)
}

class TestTimeConstraints(
  private val project: Project,
  testConfig: TimeConfig
) : TimeConstraints {
  private val multiplier: Long = testConfig.dayDurationAsNumberOfSeconds.toLong()
  override val scenario: Scenario get() =
    if (Duration.between(project.start, project.end).toSeconds() <= 13 * multiplier) Scenario.RETRO
    else Scenario.PRE_POST
  override val isMoreThan14DaysProjectDuration: Boolean get() = Duration.between(project.start, project.end).toSeconds() > (14 * multiplier)
  override val oneWeekBeforeProjectStarts: LocalDateTime get() = project.start.minusSeconds(7 * multiplier)
  override val oneWeekAfterProjectStarts: LocalDateTime get() = project.start.plusSeconds(7 * multiplier)
  override val twoWeeksAfterProjectStarts: LocalDateTime get() = project.start.plusSeconds(14 * multiplier)
  override val oneWeekBeforeProjectEnds: LocalDateTime get() = project.end.minusSeconds(7 * multiplier)
  override val oneWeekAfterProjectEnds: LocalDateTime get() = project.end.plusSeconds(7 * multiplier)
  override val twoWeeksAfterProjectEnds: LocalDateTime get() = project.end.plusSeconds(14 * multiplier)
}
