/*
 * Copyright (c) 2022 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.util

import org.slf4j.Logger

/**
 * Logs debug message on slf4j logger.
 * Note: Possible kotlin string interpolation will happen only if debug is enabled.
 * Borrowed from [kotlin-logging](https://github.com/MicroUtils/kotlin-logging)
 */
fun Logger.debug(message: () -> String) {
  if (this.isDebugEnabled) this.debug(message())
}
