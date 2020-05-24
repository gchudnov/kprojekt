package com.github.gchudnov.kprojekt.parser

import org.apache.kafka.streams.TopologyDescription
import zio.{ Has, RIO, Task, ZIO, ZLayer }

object Parser {
  type Parser = Has[Parser.Service]

  trait Service {
    def run(input: String): Task[TopologyDescription]
  }

  val any: ZLayer[Parser, Nothing, Parser] =
    ZLayer.requires[Parser]

  val live: ZLayer[Any, Nothing, Parser] = ZLayer.succeed(new LiveParser())

  def run(input: String): RIO[Parser, TopologyDescription] = ZIO.accessM(_.get.run(input))
}
