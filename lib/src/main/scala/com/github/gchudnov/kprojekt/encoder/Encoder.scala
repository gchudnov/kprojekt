package com.github.gchudnov.kprojekt.encoder

import org.apache.kafka.streams.TopologyDescription
import zio._

trait Encoder {
  def encode(name: String, desc: TopologyDescription): UIO[String]
}

object Encoder {
  def encode(name: String, desc: TopologyDescription): URIO[Encoder, String] =
    ZIO.serviceWithZIO(_.encode(name, desc))
}
