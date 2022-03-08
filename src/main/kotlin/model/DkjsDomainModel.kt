/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.model

import de.dkjs.survey.mail.MailType
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.*

enum class ScenarioType {
  PRE,
  POST,
  GOAL_G_PRE,
  GOAL_G_POST
}

@Entity
class Project(

  @Id
  @get:Pattern(regexp = "[0-9- ]+")
  val id: String,  // project.number in input data

  @get:NotEmpty
  val status: String,

  @get:NotEmpty
  val name: String,

  @OneToOne
  @get:Valid
  val provider: Provider,

  @Embedded
  @get:Valid
  val contactPerson: ContactPerson,

  @ElementCollection
  @get:NotEmpty
  val goals: Set<Int>,

  @Embedded
  @get:Valid
  val participants: Participants,

  val start: LocalDateTime,

  val end: LocalDateTime,

  @OneToOne
  var surveyProcess: SurveyProcess? = null

)

@Entity
class Provider(

  @Id
  @get:NotEmpty
  val id: String,

  @get:NotEmpty
  val name: String

)

// all the properties are nullable because they might be specified as "NA" and then null carries an information
@Embeddable
class Participants(

  @get:Min(0)
  val age1to5: Int?,

  @get:Min(0)
  val age6to10: Int?,

  @get:Min(0)
  val age11to15: Int?,

  @get:Min(0)
  val age16to19: Int?,

  @get:Min(0)
  val age20to26: Int?,

  @get:Min(0)
  val worker: Int?

)

@Embeddable
class ContactPerson(

  @get:NotEmpty
  var pronoun: String,

  @get:NotEmpty
  var firstName: String,

  @get:NotEmpty
  var lastName: String,

  @get:NotEmpty
  @get:Email
  var email: String

)

@Entity
class SurveyProcess(

  @Id
  val id: String, // should be always the same as project id

  var phase: Phase,

  @OneToMany
  val notifications: MutableList<Notification> = mutableListOf()

) {

  enum class Phase {
    CREATED,
    PERSISTED,
    FINISHED
  }

}

@Entity
class Notification(

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

interface ProviderRepository : CrudRepository<Provider, String>

interface SurveyProcessRepository : CrudRepository<SurveyProcess, String>
