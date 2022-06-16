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

  private fun mapGoals(vararg goals: Int) = mapGoalsToBlocks(goals.toSet())

}
