package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.encoder.Encoder
import com.github.gchudnov.kprojekt.formatter.Folder
import com.github.gchudnov.kprojekt.formatter.dot.{ DotConfig, DotSpace }
import com.github.gchudnov.kprojekt.naming.{ Namer, NamerConfig }
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.util.FileOps
import zio.test.Assertion._
import zio.test._
import zio.{ Has, ZIO, ZLayer }

/**
 * ProjektorSpec
 *
 * {{{
 * example:
 *   bloop test lib --only com.github.gchudnov.kprojekt.ProjektorSpec
 *
 *   cat graph.dot | dot -Tpng > graph.png
 * }}}
 */
object ProjektorSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("ProjektorSpec")(
      testM("parsing and rendering a complex topology should produce the expected graphviz output") {
        for {
          input    <- ZIO.fromEither(FileOps.stringFromResource("topologies/complex-topo-1.log"))
          desc     <- Parser.run(input).provideLayer(Parser.live)
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/complex-topo-1.dot"))
          actual   <- Encoder.encode("complex-topo-1", desc).provideLayer(defaultEnv)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      testM("parsing a complex topology should produce no duplicates for sources") {
        for {
          input    <- ZIO.fromEither(FileOps.stringFromResource("topologies/complex-topo-2.log"))
          desc     <- Parser.run(input).provideLayer(Parser.live)
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/complex-topo-2.dot"))
          actual   <- Encoder.encode("complex-topo-2", desc).provideLayer(defaultEnv)
        } yield assert(actual.trim)(equalTo(expected.trim))
      }
    )

  private val defaultDotConfig  = DotConfig(indent = 2, fontName = "sans-serif", fontSize = 10, isEmbedStore = false, hasLegend = false, space = DotSpace.Small)
  private val defaultNameConfig = NamerConfig(maxLenWithoutShortening = 12, separator = ".")

  private val defaultEnv: ZLayer[Any, Nothing, Has[Encoder.Service]] =
    withEnv(defaultDotConfig, defaultNameConfig)

  private def withEnv(dotConfig: DotConfig, nameConfig: NamerConfig): ZLayer[Any, Nothing, Has[Encoder.Service]] = {
    val dotConfigEnv  = ZLayer.succeed(dotConfig)
    val nameConfigEnv = ZLayer.succeed(nameConfig)

    val nameEnv = (nameConfigEnv >>> Namer.live)
    val foldEnv = ((dotConfigEnv ++ nameEnv) >>> Folder.live)

    val encoderEnv = foldEnv >>> Encoder.live
    encoderEnv
  }
}
