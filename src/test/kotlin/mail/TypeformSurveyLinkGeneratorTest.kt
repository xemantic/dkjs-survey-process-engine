/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.ScenarioType
import de.dkjs.survey.typeform.TypeformConfig
import de.dkjs.survey.typeform.link.TypeformSurveyLinkGenerator
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TypeformSurveyLinkGeneratorTest {

  private val config = TypeformConfig(
    clientId = "foo",
    linkBase = "https://aufleben.typeform.com/to/",
    forms = TypeformConfig.Forms(
      pre = "O5yJuV9b",
      post = "waBTYjMv",
      goalGPre = "XYMgbimk",
      goalGPost = "W1x2Lslz"
    )
  )

  @Test
  fun `should generate typeform link for 'pre' scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val projectId = "PreProject"
    val goals: Set<Int> = setOf(1, 2, 3)
    val scenarioType = ScenarioType.PRE

    // when
    val link = generator.generate(projectId, goals, scenarioType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/O5yJuV9b?project_id=PreProject&block2=b&block3=c"
  }

  @Test
  fun `should generate typeform link for 'post' scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val projectId = "PostProject"
    val goals: Set<Int> = setOf(1, 3, 4)
    val scenarioType = ScenarioType.POST

    // when
    val link = generator.generate(projectId, goals, scenarioType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv?project_id=PostProject&block2=c&block3=d"
  }

  @Test
  fun `should generate typeform link for 'goal G pre' scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val projectId = "GoalGPreProject"
    val goals: Set<Int> = setOf(1, 4, 5)
    val scenarioType = ScenarioType.GOAL_G_PRE

    // when
    val link = generator.generate(projectId, goals, scenarioType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/XYMgbimk?project_id=GoalGPreProject&block2=d&block3=e"
  }

  @Test
  fun `should generate typeform link for 'goal G post' scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val projectId = "GoalGPostProject"
    val goals: Set<Int> = setOf(1, 5)
    val scenarioType = ScenarioType.GOAL_G_POST

    // when
    val link = generator.generate(projectId, goals, scenarioType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/W1x2Lslz?project_id=GoalGPostProject&block2=e"
  }

}
