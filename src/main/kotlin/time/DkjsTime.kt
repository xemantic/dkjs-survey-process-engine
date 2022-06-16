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

package de.dkjs.survey.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * Canonical DKJS date format used in supplied CSV files and sent emails.
 */
val DKJS_DATE_FORMAT: DateTimeFormatter = DateTimeFormatterBuilder()
  .appendPattern("dd.MM.yyyy[ HH][:mm][:ss]")
  .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
  .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
  .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
  .toFormatter()

/**
 * Date formatted as `dd.MM.yyyy`.
 */
val LocalDate.dkjsDate get(): String = this.format(DKJS_DATE_FORMAT)

/**
 * Date formatted as `dd.MM.yyyy` directly from the [LocalDateTime] instance,
 * to be used when formatting emails.
 */
val LocalDateTime.dkjsDate get(): String = this.toLocalDate().dkjsDate

/**
 * Date formatted as `dd.MM.yyyy HH:mm:ss`.
 */
val LocalDateTime.dkjsDateTime get(): String = this.format(DKJS_DATE_FORMAT)

/**
 * Parses given `date` assuming `dd.MM.yyyy HH:mm:ss` format where the hour part is optional.
 * If not provided, it will default to the beginning of the day.
 */
fun parseDkjsDate(date: String): LocalDateTime = LocalDateTime.parse(date, DKJS_DATE_FORMAT)
