/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.goal

import io.kotest.matchers.collections.shouldContainAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class GoalParserTest {

  //@ParameterizedTest
  //@CsvSource
  // TODO goals will be submitted now as comma separated list like "01,02" etc.
  @Test
  fun `should parse CSV goals`() { //(input: String, expectation: String) {
    // given
    val input = "05 : Begleitung von Kindern und Jugendlichen bei der Wiederaneignung verlorengegangener Alltagsstrukturen und -erfahrungen, 04 : Förderung der Selbstlernkompetenzen von Kindern und Jugendlichen, 03 : Unterstützung des sozial-emotionalen Lernens sowie der Beziehungen und Bindungen von Kindern und Jugendlichen untereinander, 01 : Förderung der psychischen Gesundheit von Kindern und Jugendlichen (z. B. Stärken der Resilienz, emotionalen Stabilität, Selbstwirksamkeit) , 02 : Verbesserung der physischen Gesundheit von Kindern und Jugendlichen"
    val goalParser = GoalParser()

    // when
    val output = goalParser.parse(input)

    // then
    output shouldContainAll setOf(1, 2, 3, 4, 5)
  }

}
