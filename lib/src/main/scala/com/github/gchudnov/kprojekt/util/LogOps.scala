package com.github.gchudnov.kprojekt.util

import ch.qos.logback.classic.{ Level, Logger }
import org.slf4j.LoggerFactory

object LogOps {
  def setLogVerbosity(isVerbose: Boolean): Unit = {
    val rootLogger: Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]

    val logLevel =
      if (isVerbose)
        Level.TRACE
      else
        Level.INFO

    rootLogger.setLevel(logLevel)
  }
}
