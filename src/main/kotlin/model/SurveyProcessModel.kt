/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.model

import de.dkjs.survey.mail.MailType
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.*

// TODO add rest of the validation annotations
@Entity
data class Project(

  @Id
  @Pattern(regexp = "[0-1]+")
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
  val start: LocalDateTime,
  val end: LocalDateTime,
  @OneToOne
  var surveyProcess: SurveyProcess? = null
)

@Entity
data class Provider(

  @Id
  val id: String,

  @get:NotEmpty
  val name: String

)

@Embeddable
data class Participants(

  @get:Min(0)
  val age1to5: Int,

  @get:Min(0)
  val age6to10: Int,

  @get:Min(0) val
  age11to15: Int,

  @get:Min(0)
  val age16to19: Int,

  @get:Min(0)
  val age20to26: Int,

  @get:Min(0)
  val worker: Int? // TODO does it make sense to distinguish 0 and NA here?
)

@Embeddable
data class ContactPerson(
  @get:NotEmpty var pronoun: String,
  @get:NotEmpty var firstName: String,
  @get:NotEmpty var lastName: String,
  @get:NotEmpty @get:Email var email: String,
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
