/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.mail

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun parseDate(date: String) = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)
