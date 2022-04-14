/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.Project
import de.dkjs.survey.model.Scenario
import de.dkjs.survey.typeform.TypeformConfig
import de.dkjs.survey.typeform.link.TypeformSurveyLinkGenerator
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class TypeformSurveyLinkGeneratorTest {

  private val config = TypeformConfig(
    clientId = "foo",
    urlBase = "https://aufleben.typeform.com/to/",
    forms = TypeformConfig.Forms(
      retro = "O5yJuV9b",
      prePost = "waBTYjMv",
      goalGRetro = "XYMgbimk",
      goalGPrePost = "W1x2Lslz"
    )
  )

  @Test
  fun `should generate typeform link for RETRO scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = mockk<Project>()
    every { project.id } returns "PreProject"
    every { project.goals } returns listOf(1, 2, 3)
    val scenario = Scenario.RETRO

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/O5yJuV9b?project_id=PreProject&block2=b&block3=c"
  }

  @Test
  fun `should generate typeform link for PRE_POST scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = mockk<Project>()
    every { project.id } returns "PostProject"
    every { project.goals } returns listOf(1, 3, 4)
    val scenario = Scenario.PRE_POST

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv?project_id=PostProject&block2=c&block3=d"
  }

  @Test
  fun `should generate typeform link for RETRO + goal G scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = mockk<Project>()
    every { project.id } returns "GoalGPreProject"
    every { project.goals } returns listOf(1, 4, 5)
    val scenario = Scenario.RETRO

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/XYMgbimk?project_id=GoalGPreProject&block2=d&block3=e"
  }

  @Test
  fun `should generate typeform link for 'goal G post' scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = mockk<Project>()
    every { project.id } returns "GoalGPostProject"
    every { project.goals } returns listOf(1, 5)
    val scenario = Scenario.PRE_POST

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/W1x2Lslz?project_id=GoalGPostProject&block2=e"
  }

}
