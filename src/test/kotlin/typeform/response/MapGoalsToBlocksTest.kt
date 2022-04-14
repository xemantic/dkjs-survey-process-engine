/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.response

import de.dkjs.survey.typeform.link.mapGoalsToBlocks
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MapGoalsToBlocksTest {

  @Test
  fun `should map project goals to typeform survey blocks`() {
    mapGoals(1, 2) shouldBe mapOf(
      "block2" to "b"
    )
    mapGoals(1, 2, 3) shouldBe mapOf(
      "block2" to "b",
      "block3" to "c"
    )
    mapGoals(1, 3, 4) shouldBe mapOf(
      "block2" to "c",
      "block3" to "d"
    )
    mapGoals(1, 5) shouldBe mapOf(
      "block2" to "e"
    )
    mapGoals(1, 4, 5) shouldBe mapOf(
      "block2" to "d",
      "block3" to "e"
    )
    mapGoals(1, 5) shouldBe mapOf(
      "block2" to "e",
    )
  }

  private fun mapGoals(vararg goals: Int) = mapGoalsToBlocks(goals.toList())

}
