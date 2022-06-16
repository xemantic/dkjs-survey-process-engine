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

package de.dkjs.survey.mail

import de.dkjs.survey.model.SurveyType
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
      impuls = "LZewTxb9",
      pre = "O5yJuV9b",
      post = "waBTYjMv",
      goalGPre = "XYMjbimk",
      goalGPost = "W1x2LslZ"
    )
  )

  @Test
  fun `should generate typeform link for POST survey with one goal b`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 2)
    val surveyType = SurveyType.POST

    // when
    val link = generator.generate(project, surveyType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=b"
  }

  @Test
  fun `should generate typeform link for POST survey with one goal c`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 3)
    val surveyType = SurveyType.POST

    // when
    val link = generator.generate(project, surveyType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=c"
  }

  @Test
  fun `should generate typeform link for POST survey with goal b and c`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 2, 3)
    val surveyType = SurveyType.POST

    // when
    val link = generator.generate(project, surveyType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=b&block3=c"
  }

  @Test
  fun `should generate typeform link for POST survey with goal c and b`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 3, 2)
    val surveyType = SurveyType.POST

    // when
    val link = generator.generate(project, surveyType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=b&block3=c"
  }

  @Test
  fun `should generate typeform link for POST survey with goal b and d`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("RetroProject", 1, 2, 4)
    val surveyType = SurveyType.POST

    // when
    val link = generator.generate(project, surveyType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/waBTYjMv#project_id=RetroProject&block2=b&block3=d"
  }

  @Test
  fun `should generate typeform link for PRE survey`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("PrePostProject", 1, 3, 4)
    val surveyType = SurveyType.PRE

    // when
    val link = generator.generate(project, surveyType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/O5yJuV9b#project_id=PrePostProject&block2=c&block3=d"
  }

  @Test
  fun `should generate typeform link for POST + goal G survey`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("GoalGRetroProject", 1, 7)
    val surveyType = SurveyType.POST

    // when
    val link = generator.generate(project, surveyType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/W1x2LslZ#project_id=GoalGRetroProject"
  }

  @Test
  fun `should generate typeform link for PRE + goal G survey`() {
    // given
    val generator = TypeformSurveyLinkGenerator(config)
    val project = projectWithGoals("GoalGPrePostProject", 1, 7)
    val surveyType = SurveyType.PRE

    // when
    val link = generator.generate(project, surveyType)

    // then
    link shouldBe "https://aufleben.typeform.com/to/XYMjbimk#project_id=GoalGPrePostProject"
  }

}
