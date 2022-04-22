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
 * calculating execution time.
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
  override fun newTimeConstraints(project: Project) = ProductionTimeConstraints(
    project.start, project.end
  )
}

@Component
@Profile("test")
class TestTimeConstraintsFactory @Inject constructor(
  private val config: TimeConfig
) : TimeConstraintsFactory {
  override fun newTimeConstraints(project: Project) = TestTimeConstraints(
    project.start, project.end, config
  )
}

class ProductionTimeConstraints(
  private val start: LocalDateTime,
  private val end: LocalDateTime,
) : TimeConstraints {
  override val scenario: Scenario get() =
    if (Duration.between(start, end).toDays() <= 13) Scenario.RETRO else Scenario.PRE_POST
  override val isMoreThan14DaysProjectDuration: Boolean get() = Duration.between(start, end).toDays() > 14L
  override val oneWeekBeforeProjectStarts: LocalDateTime get() = start.minusWeeks(1)
  override val oneWeekAfterProjectStarts: LocalDateTime get() = start.plusWeeks(1)
  override val twoWeeksAfterProjectStarts: LocalDateTime get() = start.plusWeeks(2)
  override val oneWeekBeforeProjectEnds: LocalDateTime get() = end.minusWeeks(1)
  override val oneWeekAfterProjectEnds: LocalDateTime get() = end.plusWeeks(1)
  override val twoWeeksAfterProjectEnds: LocalDateTime get() = end.plusWeeks(2)
}

class TestTimeConstraints(
  private val start: LocalDateTime,
  private val end: LocalDateTime,
  testConfig: TimeConfig
) : TimeConstraints {
  private val multiplier: Long = testConfig.dayDurationAsNumberOfSeconds.toLong()
  override val scenario: Scenario get() =
    if (Duration.between(start, end).toSeconds() <= 13 * multiplier) Scenario.RETRO
    else Scenario.PRE_POST
  override val isMoreThan14DaysProjectDuration: Boolean get() = Duration.between(start, end).toSeconds() > (14 * multiplier)
  override val oneWeekBeforeProjectStarts: LocalDateTime get() = start.minusSeconds(7 * multiplier)
  override val oneWeekAfterProjectStarts: LocalDateTime get() = start.plusSeconds(7 * multiplier)
  override val twoWeeksAfterProjectStarts: LocalDateTime get() = start.plusSeconds(14 * multiplier)
  override val oneWeekBeforeProjectEnds: LocalDateTime get() = end.minusSeconds(7 * multiplier)
  override val oneWeekAfterProjectEnds: LocalDateTime get() = end.plusSeconds(7 * multiplier)
  override val twoWeeksAfterProjectEnds: LocalDateTime get() = end.plusSeconds(14 * multiplier)
}
