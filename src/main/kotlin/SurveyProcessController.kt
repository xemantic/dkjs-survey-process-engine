/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.model.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.inject.Inject
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull


@RestController
@Validated
open class SurveyProcessController @Inject constructor(
  private val engine: DkjsSurveyProcessEngine
) {

  @GetMapping("start")
  fun start(
    @RequestParam("projectNumber") @NotNull @NotEmpty projectNumber: String,
    @RequestParam("projectName") @NotNull @NotEmpty projectName: String,
    @RequestParam("projectContact") @NotNull @NotEmpty projectContact: String,
    @RequestParam("startDate") @NotNull @NotEmpty startDate: LocalDate,
    @RequestParam("endDate") @NotNull @NotEmpty endDate: LocalDate,
    @RequestParam("goals") @NotNull @NotEmpty goals: Set<Int>,
    @RequestParam("email") @NotNull @NotEmpty email: String,
    @RequestParam("participantCount") @NotNull @NotEmpty @Min(0) participantCount: Int
  ): ResponseEntity<String> {

    // TODO validate parameters

    if (engine.projectExists(projectNumber)) {
      return ResponseEntity(
        "Process already started, project number: $projectNumber",
        HttpStatus.CONFLICT
      )
    } else {
      engine.handleNew(Project(
        projectNumber = projectNumber,
        projectName = projectName,
        projectContact = projectContact,
        startDate = startDate,
        endDate = endDate,
        goals = goals,
        email = email,
        surveyProcess = SurveyProcess(
          phase = SurveyProcess.Phase.CREATED,
          notifications = mutableListOf()
        ),
        participantCount = participantCount
      ))
      return ResponseEntity.ok(
        "Survey process initiated for project number: $projectNumber"
      )
    }
  }

}
