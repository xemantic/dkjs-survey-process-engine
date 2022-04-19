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

  fun sendSystemAlert(message: String)

  fun sendProcessAlert(project: Project, message: String)

}

@Singleton
@Component
class DefaultAlertSender @Inject constructor(
  private val dkjsConfig: DkjsConfig,
  private val alertEmailSender: AlertEmailSender
) : AlertSender {

  override fun sendSystemAlert(message: String) {
    alertEmailSender.sendAlertEmail(
      "[${dkjsConfig.environment}] $message",
      message
    )
  }

  override fun sendProcessAlert(project: Project, message: String) {
    alertEmailSender.sendAlertEmail(
      "[${dkjsConfig.environment}] $message, " +
          "project.number: ${project.id}, project: ${project.name}",
      "$message\n\n$project"
    )
  }

}
