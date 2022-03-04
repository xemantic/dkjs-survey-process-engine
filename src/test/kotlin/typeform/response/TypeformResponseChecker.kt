/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.response

import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import de.dkjs.survey.typeform.TypeformConfig
import io.kotest.matchers.ints.shouldBeGreaterThan
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@DkjsSurveyProcessEngineTest
class TypeformResponseCheckerTest(
  @Autowired private val config: TypeformConfig,
  @Autowired private val checker: TypeformResponseChecker
) {

  @Test
  fun `should count responses in typeform for a given project`() {
    // given
    val projectId = "undine"

    // when
    val count = checker.countSurveys(config.forms.pre, projectId)

    // then
    count shouldBeGreaterThan 0
  }

}
