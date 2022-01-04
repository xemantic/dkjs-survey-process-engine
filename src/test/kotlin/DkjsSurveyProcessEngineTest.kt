/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import io.mockk.mockk
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.mail.MailSender
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@Import(TestOverridesConfiguration::class)
@ActiveProfiles("test", inheritProfiles = false)
annotation class DkjsSurveyProcessEngineTest

@TestConfiguration
open class TestOverridesConfiguration {

  @Bean
  open fun mailSender(): MailSender = mockk<MailSender>()

}
