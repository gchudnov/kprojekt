package com.github.gchudnov.kprojekt.runner

final case class RunException(message: String) extends RuntimeException(message)
