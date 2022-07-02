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

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.time.ZoneId
import javax.validation.constraints.NotNull

@Validated
@ConfigurationProperties("time")
@ConstructorBinding
data class TimeConfig(

  /**
   * Test [TimeConstraints] are operating on second basis instead of day/week basis
   * for time calculation.
   *
   * Note: will be ignored for profile `standard-time`
   */
  @get:NotNull
  val dayDurationAsNumberOfSeconds: Int,

  /**
   * The time zone used by the server for scheduling of process activities. Clouds servers
   * usually run in UTC time, so for example `Europe/Berlin` needs to be provided
   */
  val timeZone: String

) {

  val zoneId: ZoneId get() = ZoneId.of(timeZone)

}
