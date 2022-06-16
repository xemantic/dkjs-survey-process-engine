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

package de.dkjs.survey.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ModelTest {

  @Test
  fun `should convert goal to small letter`() {
    goalToSmallLetter(1) shouldBe 'a'
    goalToSmallLetter(2) shouldBe 'b'
    goalToSmallLetter(3) shouldBe 'c'
    goalToSmallLetter(4) shouldBe 'd'
    goalToSmallLetter(5) shouldBe 'e'
    goalToSmallLetter(6) shouldBe 'f'
    goalToSmallLetter(7) shouldBe 'g'
  }

  @Test
  fun `should convert goal to capital letter`() {
    goalToCapitalLetter(1) shouldBe 'A'
    goalToCapitalLetter(2) shouldBe 'B'
    goalToCapitalLetter(3) shouldBe 'C'
    goalToCapitalLetter(4) shouldBe 'D'
    goalToCapitalLetter(5) shouldBe 'E'
    goalToCapitalLetter(6) shouldBe 'F'
    goalToCapitalLetter(7) shouldBe 'G'
  }

  @Test
  fun `should return goals as a label consisting out of capital alphabet letters separated by commas`() {
    goalsToUiLabel(setOf(1)) shouldBe "A"
    goalsToUiLabel(setOf(1, 2, 3)) shouldBe "A, B, C"
    goalsToUiLabel(setOf(1, 5, 2)) shouldBe "A, B, E"
  }

  @Test
  fun `should return goals as a list of label consisting out of alphabet letters`() {
    goalsToSequenceOfSmallLetters(setOf(1)) shouldBe "a"
    goalsToSequenceOfSmallLetters(setOf(1, 2)) shouldBe "ab"
    goalsToSequenceOfSmallLetters(setOf(1, 2, 3)) shouldBe "abc"
    goalsToSequenceOfSmallLetters(setOf(1, 5, 2)) shouldBe "abe"
  }

}
