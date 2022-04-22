/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
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
   * Note: will be ignored for profile `prod`
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
