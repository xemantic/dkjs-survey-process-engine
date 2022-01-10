/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.model

import de.dkjs.survey.mail.MailType
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Project(
  @Id
  val id: String,               // project.number in input data
  val status: String,
  val name: String,
  @OneToOne
  val provider: Provider,
  @Embedded
  val contactPerson: ContactPerson,
  @ElementCollection
  val goals: Set<Int>,
  @Embedded
  val participants: Participants,
  val start: LocalDate,
  val end: LocalDate,
  @OneToOne
  val surveyProcess: SurveyProcess,
)

@Entity
data class Provider(
  @Id
  val id: String,
  val name: String
)

@Embeddable
data class Participants(
  val from1To5: Int,
  val from6To10: Int,
  val from11To15: Int,
  val from16To19: Int,
  val from20To26: Int
)

@Embeddable
data class ContactPerson(
  var pronoun: String,
  var firstName: String,
  var lastName: String,
  var email: String,
)

@Entity
data class SurveyProcess(
  @Id
  val id: String, // should be always the same as project id
  var phase: Phase,
  @OneToMany
  val notifications: MutableList<Notification>
) {

  enum class Phase {
    CREATED,
    PERSISTED,
    FINISHED
  }

}

@Entity
data class Notification(
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  val id: Int,
  val mailType: MailType,
  val sentAt: LocalDateTime = LocalDateTime.now()
)

interface ProjectRepository : CrudRepository<Project, String> {

  fun countBySurveyProcessPhase(finished: SurveyProcess.Phase): Int

  fun findBySurveyProcessPhaseNot(finished: SurveyProcess.Phase): List<Project>

}
