/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey

import de.dkjs.survey.model.*
import de.dkjs.survey.time.parseDkjsDate
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton

// TODO a simple test component to have anything in DB, should be removed soon
@Singleton
@Component
class DbPopulator @Inject constructor(
  private val providerRepository: ProviderRepository,
  private val projectRepository: ProjectRepository
) {

  @PostConstruct
  fun populateDb() {
    val provider = providerRepository.save(Provider(
      id = "123",
      name = "foo"
    ))
    val project = Project(
      name = "Foo",
      id = "42",
      contactPerson = ContactPerson(
        pronoun = "Herr",
        firstName = "Max",
        lastName = "Mustermann",
        email = "max@musterman.de"
      ),
      start = parseDkjsDate("15.01.2022"),
      end = parseDkjsDate("30.01.2022"),
      //goals = setOf(Goal.A),
      // next values will not influence mail
      //goals = setOf(Goal(1)),
      goals = setOf(1),
      //participantCount = 42,
      surveyProcess = null,
      status = "foo",
      provider = provider,
      participants = Participants(
        0, 0, 0, 0, 0,
        0
      )
    )

    projectRepository.save(project)
  }

}
