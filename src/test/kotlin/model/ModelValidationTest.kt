/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.model

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import javax.validation.Validation

class ModelValidationTest {

  @Test
  fun `should validate hierarchical project`() {
    // given
    val project = Project(
      id          = "",
      status      = "",
      name        = "",
      provider = Provider(
        id        = "",
        name      = ""
      ),
      contactPerson = ContactPerson(
        pronoun   = "",
        firstName = "",
        lastName  = "",
        email     = ""
      ),
      goals       = emptySet(),
      participants = Participants(
        age1to5   = -1,
        age6to10  = -1,
        age11to15 = -1,
        age16to19 = -1,
        age20to26 = -1,
        worker    = -1
      ),
      start       = LocalDateTime.MIN,
      end         = LocalDateTime.MIN
    )
    val validator = Validation.buildDefaultValidatorFactory().validator

    // when
    val violations = validator.validate(project)

    // then
    violations shouldHaveSize 16
    violations.map { "${it.propertyPath} - ${it.message}" }
      .shouldContainExactlyInAnyOrder(
        "id - must match \"[0-9-]+\"",
        "status - must not be empty",
        "name - must not be empty",
        "provider.id - must not be empty",
        "provider.name - must not be empty",
        "contactPerson.pronoun - must not be empty",
        "contactPerson.firstName - must not be empty",
        "contactPerson.lastName - must not be empty",
        "contactPerson.email - must not be empty",
        "goals - must not be empty",
        "participants.age1to5 - must be greater than or equal to 0",
        "participants.age6to10 - must be greater than or equal to 0",
        "participants.age11to15 - must be greater than or equal to 0",
        "participants.age16to19 - must be greater than or equal to 0",
        "participants.age20to26 - must be greater than or equal to 0",
        "participants.worker - must be greater than or equal to 0"
      )
  }

}
