/*
 * dkjs-survey-process-engine - https://www.dkjs.de/
 * Copyright (C) 2022 Kazimierz Pogoda / https://xemantic.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.dkjs.survey.time

import de.dkjs.survey.model.Project
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
  val projectDurationInDays: Int
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
@Profile("standard-time")
class StandardTimeConstraintsFactory : TimeConstraintsFactory {
  override fun newTimeConstraints(project: Project) = StandardTimeConstraints(
    project.start, project.end
  )
}

@Component
@Profile("compressed-time")
class CompressedTimeConstraintsFactory @Inject constructor(
  private val config: TimeConfig
) : TimeConstraintsFactory {
  override fun newTimeConstraints(project: Project) = CompressedTimeConstraints(
    project.start, project.end, config
  )
}

class StandardTimeConstraints(
  private val start: LocalDateTime,
  private val end: LocalDateTime,
) : TimeConstraints {
  override val projectDurationInDays: Int = Duration.between(start, end).toDays().toInt()
  override val oneWeekBeforeProjectStarts: LocalDateTime get() = start.minusWeeks(1)
  override val oneWeekAfterProjectStarts: LocalDateTime get() = start.plusWeeks(1)
  override val twoWeeksAfterProjectStarts: LocalDateTime get() = start.plusWeeks(2)
  override val oneWeekBeforeProjectEnds: LocalDateTime get() = end.minusWeeks(1)
  override val oneWeekAfterProjectEnds: LocalDateTime get() = end.plusWeeks(1)
  override val twoWeeksAfterProjectEnds: LocalDateTime get() = end.plusWeeks(2)
}

class CompressedTimeConstraints(
  private val start: LocalDateTime,
  private val end: LocalDateTime,
  testConfig: TimeConfig
) : TimeConstraints {
  private val multiplier: Long = testConfig.dayDurationAsNumberOfSeconds.toLong()
  override val projectDurationInDays: Int = (Duration.between(start, end).toSeconds() / multiplier).toInt()
  override val oneWeekBeforeProjectStarts: LocalDateTime get() = start.minusSeconds(7 * multiplier)
  override val oneWeekAfterProjectStarts: LocalDateTime get() = start.plusSeconds(7 * multiplier)
  override val twoWeeksAfterProjectStarts: LocalDateTime get() = start.plusSeconds(14 * multiplier)
  override val oneWeekBeforeProjectEnds: LocalDateTime get() = end.minusSeconds(7 * multiplier)
  override val oneWeekAfterProjectEnds: LocalDateTime get() = end.plusSeconds(7 * multiplier)
  override val twoWeeksAfterProjectEnds: LocalDateTime get() = end.plusSeconds(14 * multiplier)
}
