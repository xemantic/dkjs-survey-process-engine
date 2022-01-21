/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Canonical DKJS date format used in supplied CSV files and sent emails.
 */
val DKJS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy[ HH:mm:ss]")

/**
 * Date formatted as dd.MM.yyyy
 */
val LocalDate.dkjsDate get(): String = this.format(DKJS_DATE_FORMAT)

/**
 * Date formatted as dd.MM.yyyy directly from the [LocalDateTime] instance,
 * to be used when formatting emails.
 */
val LocalDateTime.dkjsDate get(): String = this.toLocalDate().dkjsDate

/**
 * Date formatted as dd.MM.yyyy HH:mm:ss
 */
val LocalDateTime.dkjsDateTime get(): String = this.format(DKJS_DATE_FORMAT)

fun parseDkjsDate(date: String) = LocalDateTime.parse(date, DKJS_DATE_FORMAT)
