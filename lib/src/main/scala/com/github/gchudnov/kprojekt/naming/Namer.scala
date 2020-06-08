package com.github.gchudnov.kprojekt.naming

import zio._

object Namer {
  type Namer = Has[Namer.Service]

  trait Service {
    def name(input: String): UIO[NodeName]
  }

  val any: ZLayer[Namer, Nothing, Namer] =
    ZLayer.requires[Namer]

  val live: ZLayer[NameConfig, Nothing, Namer] =
    ZLayer.fromFunction(config => new LiveNamer(config))

  def name(input: String): URIO[Namer, NodeName] = ZIO.accessM(_.get.name(input))
}
