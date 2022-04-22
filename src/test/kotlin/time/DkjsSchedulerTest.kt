/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
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
class DkjsSchedulerTest @Autowired constructor(
  val scheduler: DkjsScheduler
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
    duration shouldBeGreaterThanOrEqualTo 1000
  }

}
