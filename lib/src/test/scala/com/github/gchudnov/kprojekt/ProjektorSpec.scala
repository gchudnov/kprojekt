package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.encoder.Encoder
import com.github.gchudnov.kprojekt.formatter.Folder
import com.github.gchudnov.kprojekt.formatter.dot.DotConfig
import com.github.gchudnov.kprojekt.naming.{ NameConfig, Namer }
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.util.FileOps
import zio.test.Assertion._
import zio.test._
import zio.{ Has, ZIO, ZLayer }

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

  def withConfig(c: DotConfig): ZLayer[Any, Nothing, DotConfig] =
    ZLayer.succeedMany(c)

  private val defaultDotConfig  = DotConfig(indent = 2, fontName = "sans-serif", fontSize = 10, isEmbedStore = false, hasLegend = false)
  private val defaultNameConfig = NameConfig(maxLenWithoutShortening = 12, separator = ".")

  private val defaultEnv: ZLayer[Any, Nothing, Has[Encoder.Service]] =
    withEnv(defaultDotConfig, defaultNameConfig)

  private def withEnv(dotConfig: DotConfig, nameConfig: NameConfig): ZLayer[Any, Nothing, Has[Encoder.Service]] = {
    val dotConfigEnv  = ZLayer.succeedMany(dotConfig)
    val nameConfigEnv = ZLayer.succeedMany(nameConfig)

    val nameEnv = (nameConfigEnv >>> Namer.live)
    val foldEnv = (dotConfigEnv >>> Folder.live)

    val encoderEnv = (nameEnv ++ foldEnv) >>> Encoder.live
    encoderEnv
  }
}
