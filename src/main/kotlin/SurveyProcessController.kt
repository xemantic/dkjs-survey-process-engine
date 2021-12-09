/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.mail.EmailService
import de.dkjs.survey.typeform.TypeformSurveyLinkGenerator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject

@RestController
class SurveyProcessController @Inject constructor(
  private val linkGenerator: TypeformSurveyLinkGenerator,
  private val emailService: EmailService,
) {

  @GetMapping("start")
  fun start(
    @RequestParam("projectId") projectId: String,
    @RequestParam("blocks") blocks: String,
    @RequestParam("email") email: String
  ): String {
    val link = linkGenerator.generate(projectId, blocks)
    emailService.sendEmail(email, link)
    return "process started"
  }

}
