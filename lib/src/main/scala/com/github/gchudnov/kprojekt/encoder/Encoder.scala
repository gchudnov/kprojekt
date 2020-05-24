package com.github.gchudnov.kprojekt.encoder

import com.github.gchudnov.kprojekt.formatter.Folder.Folder
import com.github.gchudnov.kprojekt.formatter.Folder
import org.apache.kafka.streams.TopologyDescription
import zio.{ Has, UIO, URIO, ZIO, ZLayer }

object Encoder {
  type Encoder = Has[Encoder.Service]

  trait Service {
    def encode(name: String, desc: TopologyDescription): UIO[String]
  }

  val any: ZLayer[Encoder, Nothing, Encoder] =
    ZLayer.requires[Encoder]

  val live: ZLayer[Folder, Nothing, Encoder] = ZLayer.fromService { (folder: Folder.Service) =>
    new LiveEncoder(folder)
  }

  def encode(name: String, desc: TopologyDescription): URIO[Encoder, String] = ZIO.accessM(_.get.encode(name, desc))
}
