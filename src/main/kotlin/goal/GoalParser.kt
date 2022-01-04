/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.goal

import org.springframework.stereotype.Component
import javax.inject.Singleton

@Singleton
@Component
class GoalParser {

  val captureGoalRegex = Regex("[0-9]{2} : \\w+(\\s+\\w+)*")
  val captureGoalNumberRegex = Regex("([0-9][1-7]) : .*(:?)")

  fun parse(input: String): Set<Int> = captureGoalRegex.findAll(input).map { goal ->
      val goalNumber = captureGoalNumberRegex.find(goal.value)!!.destructured.component1()
      Integer.parseInt(goalNumber)
    }.toSet()

}
