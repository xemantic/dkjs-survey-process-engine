/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
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
