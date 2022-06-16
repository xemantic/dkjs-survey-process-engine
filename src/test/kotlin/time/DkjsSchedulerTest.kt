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

import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@DkjsSurveyProcessEngineTest
class DkjsSchedulerTest(
    @Autowired private val scheduler: DkjsScheduler
) {

  @Test
  fun `should execute immediately on executeNow`() {
    // given
    val passed = AtomicBoolean(false)

    // when
    scheduler.executeNow {
      passed.set(true)
    }
    Thread.sleep(100)

    // then
    passed.get() shouldBe true
  }

  @Test
  fun `should schedule immediately for date in the past`() {
    // given
    val past = LocalDateTime.now().minusYears(42)
    val passed = AtomicBoolean(false)

    // when
    scheduler.schedule(past) {
      passed.set(true)
    }
    Thread.sleep(100)

    // then
    passed.get() shouldBe true
  }

  @Test
  fun `should schedule immediately for now`() {
    // given
    val now = LocalDateTime.now()
    val passed = AtomicBoolean(false)

    // when
    scheduler.schedule(now) {
      passed.set(true)
    }
    Thread.sleep(100)

    // then
    passed.get() shouldBe true
  }

  @Test
  fun `should schedule in 1 second`() {
    // given
    val now = LocalDateTime.now()
    val inOneSecond = now.plusSeconds(1)
    val passed = AtomicReference<LocalDateTime>(null)

    // when
    scheduler.schedule(inOneSecond) {
      passed.set(LocalDateTime.now())
    }
    Thread.sleep(2000) // wait 2 sec

    // then
    val actionTime = passed.get()
    actionTime shouldNotBe null
    val duration = Duration.between(now, actionTime).toMillis()
    duration shouldBeGreaterThanOrEqualTo 995 // sometimes 1000 is too restrictive
  }

  @Test
  fun `should not trigger scheduled action before scheduled time`() {
    // given
    val now = LocalDateTime.now()
    val inOneSecond = now.plusSeconds(1)
    val passed = AtomicReference(false)

    // when
    scheduler.schedule(inOneSecond) {
      passed.set(true)
    }
    Thread.sleep(500) // wait .5 sec

    // then
    passed.get() shouldBe false
  }

}
