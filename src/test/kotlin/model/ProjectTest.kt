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

package de.dkjs.survey.model

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import javax.validation.Validation

class ProjectTest {

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
    violations shouldHaveSize 18
    violations
      .map { "${it.propertyPath} - ${it.message}" }
      .sorted()
      .shouldContainExactly(
        "contactPerson.email - must not be empty",
        "contactPerson.firstName - must not be empty",
        "contactPerson.lastName - must not be empty",
        "contactPerson.pronoun - must not be empty",
        "goals - must be a set of integer numbers within 1..7 range and 1 must be always present",
        "goals - must not be empty",
        "goals - size must be between 1 and 3",
        "id - must not be empty",
        "name - must not be empty",
        "participants.age11to15 - must be greater than or equal to 0",
        "participants.age16to19 - must be greater than or equal to 0",
        "participants.age1to5 - must be greater than or equal to 0",
        "participants.age20to26 - must be greater than or equal to 0",
        "participants.age6to10 - must be greater than or equal to 0",
        "participants.worker - must be greater than or equal to 0",
        "provider.id - must not be empty",
        "provider.name - must not be empty",
        "status - must not be empty"
      )
  }

}
