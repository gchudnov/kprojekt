package com.github.gchudnov.kprojekt.parser

import org.apache.kafka.streams.TopologyDescription
import zio._

trait Parser {
  def run(input: String): Task[TopologyDescription]
}

object Parser {

  def run(input: String): RIO[Parser, TopologyDescription] =
    ZIO.serviceWithZIO(_.run(input))
}
