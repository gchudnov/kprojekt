package com.github.gchudnov.kprojekt

import java.io.File

import com.github.gchudnov.kprojekt.encoder.Encoder
import com.github.gchudnov.kprojekt.formatter.Bundler
import com.github.gchudnov.kprojekt.parser.Parser
import zio.{ Has, Task, ZIO, ZLayer }

object Projektor {
  type Projektor = Has[Projektor.Service]

  trait Service {
    def run(topologyFile: File): Task[File]
  }

  val any: ZLayer[Projektor, Nothing, Projektor] =
    ZLayer.requires[Projektor]

  val live: ZLayer[Has[Parser.Service] with Has[Encoder.Service] with Has[Bundler.Service], Nothing, Has[Service]] =
    ZLayer.fromServices[Parser.Service, Encoder.Service, Bundler.Service, Projektor.Service] { (parser: Parser.Service, encoder: Encoder.Service, bundler: Bundler.Service) =>
      new LiveProjector(parser, encoder, bundler)
    }

  def run(topologyFile: File): ZIO[Projektor, Throwable, File] =
    ZIO.accessM(_.get.run(topologyFile))
}
