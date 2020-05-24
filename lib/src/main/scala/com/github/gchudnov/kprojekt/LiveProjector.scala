package com.github.gchudnov.kprojekt
import java.io.File

import com.github.gchudnov.kprojekt.encoder.Encoder
import com.github.gchudnov.kprojekt.formatter.Bundler
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.util.FileOps
import zio.{ Task, ZIO }

/**
 * Given Topology File, creates an output file with projected topology.
 */
final class LiveProjector(parser: Parser.Service, encoder: Encoder.Service, bundler: Bundler.Service) extends Projektor.Service {
  override def run(topologyFile: File): Task[File] =
    for {
      input   <- ZIO.fromEither(FileOps.stringFromFile(topologyFile))
      desc    <- parser.run(input)
      encoded <- encoder.encode(topologyFile.getName, desc)
      file    <- bundler.bundle(topologyFile, encoded)
    } yield file
}
