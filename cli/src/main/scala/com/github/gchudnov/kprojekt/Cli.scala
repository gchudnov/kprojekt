package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.encoder.LiveEncoder
import com.github.gchudnov.kprojekt.formatter.FolderConfig
import com.github.gchudnov.kprojekt.formatter.dot.{ DotBundler, DotFolder }
import com.github.gchudnov.kprojekt.naming.{ LiveNamer, NamerConfig }
import com.github.gchudnov.kprojekt.parser.LiveParser
import scopt.{ OParser, OParserBuilder }
import zio.Console.printLineError
import zio.{ Has, ZEnv, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer }

import java.io.File

object Cli extends ZIOAppDefault {

  final case class AppConfig(topologyFile: File = new File("."), space: String = "medium", isVerbose: Boolean = false)

  val builder: OParserBuilder[AppConfig] = OParser.builder[AppConfig]

  val parser: OParser[Unit, AppConfig] = {
    import builder._
    OParser.sequence(
      programName(BuildInfo.name),
      head(BuildInfo.name, BuildInfo.version),
      help("help").text("prints this usage text"),
      opt[Unit]("verbose")
        .action((_, c) => c.copy(isVerbose = true))
        .text("verbose mode"),
      opt[String]("space")
        .action((x, c) => c.copy(space = x))
        .text("space between nodes: [small,s; medium,m; large,l] (default: m)"),
      arg[File]("<file>")
        .required()
        .action((x, c) => c.copy(topologyFile = x))
        .text("path to topology description"),
      version("version")
    )
  }

  override def run: ZIO[Environment with ZEnv with Has[ZIOAppArgs], Any, Any] = {
    val program: ZIO[Has[ZIOAppArgs], Throwable, Unit] = for {
      as     <- args
      config <- ZIO.attempt(OParser.parse(parser, as, AppConfig())).flatMap(c => ZIO.fromOption(c).orElseFail(new RuntimeException("Arguments Configuration cannot be created")))
      env     = makeEnv(config)
      _      <- Projektor.run(config.topologyFile).provideLayer(env)
    } yield ()

    program
      .tapError(t => printLineError(s"Error: ${t.getMessage}"))
  }

  private def makeEnv(c: AppConfig): ZLayer[Any, Throwable, Has[Projektor]] = {
    val parseEnv  = LiveParser.layer
    val nameEnv   = NamerConfig.layer >>> LiveNamer.layer
    val foldEnv   = (FolderConfig.make(c.space) ++ nameEnv) >>> DotFolder.layer
    val encEnv    = nameEnv ++ foldEnv >>> LiveEncoder.layer
    val bundleEnv = DotBundler.layer(c.isVerbose)
    val projEnv   = (parseEnv ++ encEnv ++ bundleEnv) >>> LiveProjector.layer

    projEnv
  }
}
