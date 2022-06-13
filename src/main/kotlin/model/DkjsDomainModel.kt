/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.*
import javax.validation.constraints.*
import kotlin.reflect.KClass

enum class SurveyType {
  IMPULS,
  PRE,
  POST
}

@Entity
class Project(

  @Id
  @get:NotEmpty
  var id: String,  // project.number in input data

  @get:NotEmpty
  var status: String,

  @get:NotEmpty
  var name: String,

  @OneToOne(
    cascade = [CascadeType.ALL],
    fetch = FetchType.EAGER
  )
  @get:Valid
  var provider: Provider,

  @Embedded
  @get:Valid
  var contactPerson: ContactPerson,

  @ElementCollection(fetch = FetchType.EAGER)
  @get:NotEmpty
  @get:Size(min = 1, max = 3)
  @get:ValidGoalIds
  var goals: Set<Int>,

  @Embedded
  @get:Valid
  var participants: Participants,

  @get:NotNull
  var start: LocalDateTime,

  @get:NotNull
  var end: LocalDateTime,

  @OneToOne(
    cascade = [CascadeType.ALL],
    fetch = FetchType.EAGER
  )
  var surveyProcess: SurveyProcess? = null

) {

  val isGoalG: Boolean get() = goals.contains(7)

}

@Entity
class Provider(

  @Id
  @get:NotEmpty
  var id: String,

  @get:NotEmpty
  var name: String

)

// all the properties are nullable because they might be specified as "NA" and then null carries an information
@Embeddable
class Participants(

  @Column(name = "participants_age1to5")
  @get:Min(0)
  var age1to5: Int?,

  @Column(name = "participants_age6to10")
  @get:Min(0)
  var age6to10: Int?,

  @Column(name = "participants_age11to15")
  @get:Min(0)
  var age11to15: Int?,

  @Column(name = "participants_age16to19")
  @get:Min(0)
  var age16to19: Int?,

  @Column(name = "participants_age20to26")
  @get:Min(0)
  var age20to26: Int?,

  @Column(name = "participants_worker")
  @get:Min(0)
  var worker: Int?

)

@Embeddable
class ContactPerson(

  @Column(name = "contact_pronoun")
  @get:NotEmpty
  var pronoun: String,

  @Column(name = "contact_first_name")
  @get:NotEmpty
  var firstName: String,

  @Column(name = "contact_last_name")
  @get:NotEmpty
  var lastName: String,

  @Column(name = "contact_email")
  @get:NotEmpty
  @get:Email
  var email: String

)

@Entity
class SurveyProcess(

  @Id
  var id: String, // should be always the same as project id

  var start: LocalDateTime,

  @Enumerated(EnumType.STRING)
  var phase: Phase,

  @OneToMany(
    cascade = [CascadeType.ALL],
    mappedBy = "surveyProcessId",
    fetch = FetchType.EAGER
  )
  @OrderBy("executedAt")
  var activities: MutableList<Activity> = mutableListOf()

) {

  enum class Phase {
    ACTIVE,
    FAILED,
    FINISHED
  }

  fun alreadyExecuted(name: String): Boolean =
    activities.any { it.name == name }

}

/**
 * [Activity]'s composite key.
 *
 * Note: It's defined as kotlin data class to be Serializable and have proper
 * hashCode / equals generated, as required by JPA specs. Marking it as @Embeddable
 * is not required by JPA but will result in generating additional no-args constructor
 * required by JPA provider.
 */
@Embeddable
data class ActivityId(
  val surveyProcessId: String,
  val name: String
) : java.io.Serializable

@Entity
@IdClass(ActivityId::class)
class Activity(

  @Id
  @JsonIgnore
  val surveyProcessId: String,

  @Id
  val name: String,

  @Suppress("unused") // use in JPA sorting criteria
  var executedAt: LocalDateTime = LocalDateTime.now(),

  @Suppress("unused") // REST api display (through reflection)
  var result: String? = null,

  var failure: String? = null

) {

  @get:JsonIgnore
  val failed: Boolean get() = (failure != null)

}

fun goalsToUiLabel(goals: Set<Int>): String =
  goals
    .sorted()
    .map { goalToCapitalLetter(it) }
    .joinToString(", ")

fun goalsToSequenceOfSmallLetters(goals: Set<Int>): String =
  goals
    .sorted()
    .map { goalToSmallLetter(it) }
    .joinToString("")

fun goalToCapitalLetter(goal: Int): Char = goalToLetter(goal, 64)

fun goalToSmallLetter(goal: Int): Char = goalToLetter(goal, 96)

fun goalToLetter(goal: Int, charOffset: Int): Char =
  (goal + charOffset).toChar()

interface ProjectRepository : CrudRepository<Project, String> {

  fun countBySurveyProcessPhase(phase: SurveyProcess.Phase): Int

  fun findBySurveyProcessPhase(phase: SurveyProcess.Phase): List<Project>

}

interface ProviderRepository : CrudRepository<Provider, String>

@Suppress("unused") // used automagically by spring-data-rest
interface SurveyProcessRepository : CrudRepository<SurveyProcess, String>

@Suppress("unused") // used automagically by spring-data-rest
@RepositoryRestResource(exported = false)
interface ActivityRepository : CrudRepository<Activity, Int>

@MustBeDocumented
@Constraint(validatedBy = [GoalIdsValidator::class])
@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidGoalIds(
  val message: String = "must be a set of integer numbers within 1..7 range and 1 must be always present",
  @Suppress("unused") // used by javax.validation internally
  val groups: Array<KClass<*>> = [],
  @Suppress("unused") // used by javax.validation internally
  val payload: Array<KClass<out Payload>> = []
)

class GoalIdsValidator : ConstraintValidator<ValidGoalIds, Set<Int>> {

  private val allowedGoalRange = 1..7

  override fun initialize(contactNumber: ValidGoalIds) {}

  override fun isValid(
    goals: Set<Int>,
    cxt: ConstraintValidatorContext
  ): Boolean = goals.contains(1) and goals.all {
    allowedGoalRange.contains(it)
  }

}
