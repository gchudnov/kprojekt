package com.github.gchudnov.kprojekt.parser

import org.apache.kafka.streams.TopologyDescription
import zio.{ Has, RIO, Task, ZIO }

trait Parser {
  def run(input: String): Task[TopologyDescription]
}

object Parser {

  def run(input: String): RIO[Has[Parser], TopologyDescription] =
    ZIO.serviceWith(_.run(input))
}
