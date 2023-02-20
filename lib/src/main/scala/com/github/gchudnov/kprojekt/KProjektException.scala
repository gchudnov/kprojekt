package com.github.gchudnov.kprojekt

class KProjektException(message: String) extends RuntimeException(message) {

  def this(message: String, cause: Throwable) = {
    this(message)
    initCause(cause)
  }

  def this(cause: Throwable) =
    this(Option(cause).map(_.toString).orNull, cause)

  def this() =
    this(null: String)
}

object KProjektException {
  def unapply(e: KProjektException): Option[(String, Throwable)] =
    Some((e.getMessage, e.getCause))
}
