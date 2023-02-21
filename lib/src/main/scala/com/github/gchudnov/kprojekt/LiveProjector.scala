package com.github.gchudnov.kprojekt
// import com.github.gchudnov.kprojekt.encoder.Encoder
import com.github.gchudnov.kprojekt.formatter.Bundler
import com.github.gchudnov.kprojekt.input.Parser
import com.github.gchudnov.kprojekt.util.Files
import zio._

import java.io.File

/**
 * Given Topology File, creates an output file with projected topology.
 */
final class LiveProjector() extends Projektor {
  override def run(topologyFile: File): Task[File] =
    // for {
    //   input   <- ZIO.fromEither(Files.stringFromFile(topologyFile))
    //   desc    <- parser.run(input)
    //   encoded <- encoder.encode(topologyFile.getName, desc)
    //   file    <- bundler.bundle(topologyFile, encoded)
    // } yield file
    ???
}

// parser: Parser, encoder: Encoder, bundler: Bundler

object LiveProjector {

  // def layer: ZLayer[Parser with Encoder with Bundler, Nothing, Projektor] =
  //   ZLayer(for {
  //     parser  <- ZIO.service[Parser]
  //     encoder <- ZIO.service[Encoder]
  //     bundler <- ZIO.service[Bundler]
  //     service  = new LiveProjector(parser, encoder, bundler)
  //   } yield service)

}
