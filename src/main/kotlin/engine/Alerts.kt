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
