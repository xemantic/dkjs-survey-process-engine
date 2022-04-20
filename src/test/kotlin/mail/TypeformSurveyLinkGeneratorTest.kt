/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import de.dkjs.survey.model.Scenario
import de.dkjs.survey.test.projectWithGoals
import de.dkjs.survey.typeform.TypeformConfig
import de.dkjs.survey.typeform.link.TypeformSurveyLinkGenerator
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TypeformSurveyLinkGeneratorTest {

  private val config = TypeformConfig(
    clientId = "foo",
    urlBase = "https://aufleben.typeform.com/to/",
    forms = TypeformConfig.Forms(
      retro = "waBTYjMv",
      prePost = "O5yJuV9b",
      goalGRetro = "W1x2Lslz",
      goalGPrePost = "XYMgbimk"
    )
  )

  @Test
  fun `should generate typeform link for RETRO scenario with one goal b`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 2)
    val scenario = Scenario.RETRO

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=b"
  }

  @Test
  fun `should generate typeform link for RETRO scenario with one goal c`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 3)
    val scenario = Scenario.RETRO

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=c"
  }

  @Test
  fun `should generate typeform link for RETRO scenario with goal b and c`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 2, 3)
    val scenario = Scenario.RETRO

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=b&block3=c"
  }

  @Test
  fun `should generate typeform link for RETRO scenario with goal c and b`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 3, 2)
    val scenario = Scenario.RETRO

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=b&block3=c"
  }

  @Test
  fun `should generate typeform link for RETRO scenario with goal b and d`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 2, 4)
    val scenario = Scenario.RETRO

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=b&block3=d"
  }

  @Test
  fun `should generate typeform link for PRE_POST scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("PrePostProject", 1, 3, 4)
    val scenario = Scenario.PRE_POST

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/O5yJuV9b#project_id=PrePostProject&block2=c&block3=d"
  }

  @Test
  fun `should generate typeform link for RETRO + goal G scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("GoalGRetroProject", 1, 7)
    val scenario = Scenario.RETRO

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/W1x2Lslz#project_id=GoalGRetroProject"
  }

  @Test
  fun `should generate typeform link for PRE_POST + goat G scenario`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("GoalGPrePostProject", 1, 7)
    val scenario = Scenario.PRE_POST

    // when
    val link = generator.generate(project, scenario)

    // then
    link shouldBe "https://aufleben.typeform.com/to/XYMgbimk#project_id=GoalGPrePostProject"
  }

}
