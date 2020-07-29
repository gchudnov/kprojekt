package com.github.gchudnov.kprojekt.naming

import zio._

object Namer {
  type Namer = Has[Namer.Service]

  trait Service {
    def name(input: String): UIO[NodeName]
    def get(input: String): NodeName
  }

  val any: ZLayer[Namer, Nothing, Namer] =
    ZLayer.requires[Namer]

  val live: ZLayer[Has[NamerConfig], Nothing, Has[Service]] =
    ZLayer.fromService[NamerConfig, Namer.Service]((config: NamerConfig) => new LiveNamer(config))

  def name(input: String): URIO[Namer, NodeName] = ZIO.accessM(_.get.name(input))
}
