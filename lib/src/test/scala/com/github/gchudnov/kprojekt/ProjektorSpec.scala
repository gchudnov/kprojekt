package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.encoder.Encoder
import com.github.gchudnov.kprojekt.formatter.{ Folder, FolderConfig }
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.util.FileOps
import zio.ZIO
import zio.test.Assertion._
import zio.test._

/**
 * ProjektorSpec
 *
 * example:
 *   bloop test lib --only com.github.gchudnov.kprojekt.ProjektorSpec
 *
 *   cat graph.dot | dot -Tpng > graph.png
 */
object ProjektorSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("ProjektorSpec")(
      testM("parsing and rendering a complex topology should produce the expected graphviz output") {
        for {
          input    <- ZIO.fromEither(FileOps.stringFromResource("topologies/complex-topo.log"))
          desc     <- Parser.run(input).provideLayer(Parser.live)
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/complex-topo.dot"))
          actual   <- Encoder.encode("complex-topo", desc).provideLayer(Encoder.live).provideLayer(Folder.live).provideLayer(FolderConfig.live)
        } yield assert(actual.trim)(equalTo(expected.trim))
      }
    )
}
