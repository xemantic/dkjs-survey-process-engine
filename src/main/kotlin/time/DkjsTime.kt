/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.time

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Canonical DKJS date format used in supplied CSV files and sent emails.
 */
val DKJS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd[ HH:mm:ss]")

val LocalDateTime.dkjsDate get(): String = this.format(DKJS_DATE_FORMAT)
