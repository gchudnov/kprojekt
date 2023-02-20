package com.github.gchudnov.kprojekt.input

import com.github.gchudnov.kprojekt.input.internal.FastParser
import org.apache.kafka.streams.TopologyDescription
import zio._

trait Parser {
  def parse(input: String): Task[TopologyDescription]
}

object Parser {
  def parse(input: String): RIO[Parser, TopologyDescription] =
    ZIO.serviceWithZIO(_.parse(input))

  def make(): ULayer[Parser] =
    ZLayer.succeed(new FastParser())
}
