package com.github.gchudnov.kprojekt.encoder

import com.github.gchudnov.kprojekt.formatter.Folder
import com.github.gchudnov.kprojekt.naming.Namer
import org.apache.kafka.streams.TopologyDescription
import zio._

object Encoder {
  type Encoder = Has[Encoder.Service]

  trait Service {
    def encode(name: String, desc: TopologyDescription): UIO[String]
  }

  val any: ZLayer[Encoder, Nothing, Encoder] =
    ZLayer.requires[Encoder]

  val live: ZLayer[Has[Folder.Service] with Has[Namer.Service], Nothing, Has[Service]] = ZLayer.fromServices[Folder.Service, Namer.Service, Encoder.Service] {
    (folder: Folder.Service, namer: Namer.Service) =>
      new LiveEncoder(folder, namer)
  }

  def encode(name: String, desc: TopologyDescription): URIO[Encoder, String] = ZIO.accessM(_.get.encode(name, desc))
}
