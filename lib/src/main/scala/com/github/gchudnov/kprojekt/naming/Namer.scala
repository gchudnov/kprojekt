package com.github.gchudnov.kprojekt.naming

import zio._

trait Namer {
  def name(input: String): UIO[NodeName]
  def get(input: String): NodeName
}

object Namer {
  def name(input: String): URIO[Has[Namer], NodeName] =
    ZIO.serviceWith(_.name(input))
}
