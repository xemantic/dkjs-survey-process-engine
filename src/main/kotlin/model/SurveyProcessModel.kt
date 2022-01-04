/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.model

import de.dkjs.survey.mail.MailType
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@JvmInline
value class Goal(val goalNumber: Int) {
  init {
    require(goalNumber in 1..7) { // TODO is it true?
      "Goal number must be in range [-90, 90], but was: $goalNumber"
    }
  }
}


@Entity
data class Project(
  @Id
  val projectNumber: String,
  val projectName: String,
  val projectContact: String,
  val startDate: LocalDate,
  val endDate: LocalDate,
  val email: String,
  @ElementCollection
  val goals: Set<Int>,
  val participantCount: Int,
  @Embedded
  val surveyProcess: SurveyProcess
) {
//  @ElementCollection(targetClass=Goal.class)
//    @Enumerated(EnumType.STRING) // Possibly optional (I'm not sure) but defaults to ORDINAL.
//    @CollectionTable(name="person_interest")
//    @Column(name="goal") // Column name in person_interest
//    var goals: Set<Goal>? = null

}

@Embeddable
data class SurveyProcess(
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
