/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.engine

import de.dkjs.survey.mail.AlertEmailSender
import de.dkjs.survey.model.Project
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton

interface AlertSender {

  fun sendSystemAlert(message: String, details: String)

  fun sendProcessAlert(message: String, project: Project)

}

@Singleton
@Component
class DefaultAlertSender @Inject constructor(
  private val dkjsConfig: DkjsConfig,
  private val alertEmailSender: AlertEmailSender
) : AlertSender {

  override fun sendSystemAlert(message: String, details: String) {
    alertEmailSender.sendAlertEmail(
      subject = "[${dkjsConfig.environment}] $message",
      body = """
        $message

        $details
      """.trimIndent()
    )
  }

  override fun sendProcessAlert(message: String, project: Project) {
    val contact = project.contactPerson
    alertEmailSender.sendAlertEmail(
      subject = "[${dkjsConfig.environment}] $message, " +
          "project.number: ${project.id}, project: ${project.name}",
      body = """
        $message

        Project details:
        - number: ${project.id}
        - name: ${project.name}
        - status: ${project.status}
        - goals: ${project.goals}
        - start: ${project.start}
        - end: ${project.end}
        - contact: ${contact.pronoun} ${contact.firstName} ${contact.lastName}
        - email: ${contact.email}
        - provider number: ${project.provider.id}
        - provider name: ${project.provider.id}
      """.trimIndent()
    )
  }

}
