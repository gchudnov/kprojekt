package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.encoder.LiveEncoder
import com.github.gchudnov.kprojekt.formatter.dot.{ DotBundler, DotFolder }
import com.github.gchudnov.kprojekt.naming.LiveNamer
// import com.github.gchudnov.kprojekt.parser.LiveParser
import com.github.gchudnov.kprojekt.{ BuildInfo => KBuildInfo }
import zio.cli.HelpDoc.Span.text
import zio.Console._
import zio._
import zio.logging._
import zio.cli._
import java.nio.file.Path

object Cli extends ZIOCliDefault {

  case class KOptions(scale: Boolean, verbose: Boolean, version: Boolean)

  val scaleFlag: Options[Boolean] = Options.boolean("scale").alias("s")
  val verboseFlag: Options[Boolean] = Options.boolean("verbose").alias("v")
  val versionFlag: Options[Boolean] = Options.boolean("version")

  val options = (scaleFlag ++ verboseFlag ++ versionFlag).as(KOptions)

  val inputTopologyArgs: Args[Path] = Args.file("input-topology", Exists.Yes)

  val args = inputTopologyArgs

  val rootCommand = Command(KBuildInfo.name, options, args)
  

  val cliApp = CliApp.make(
    name = "KProjekt Cli",
    version = KBuildInfo.version,
    summary = text("Visualize Kafka Topology"),
    command = rootCommand
  ) {
    case _ =>
      printLine(s"Good Job")
  }

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = 
    Runtime.removeDefaultLoggers ++ console(LogFormat.default)

  // override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = {
  //   val osetup: ZLayer[Any, Throwable, OZEffectSetup] = makeOZEffectSetup()
  //   val psetup: OParserSetup                          = makePEffectSetup()

  //   val program = for {
  //     as  <- getArgs
  //     cfg <- CliConfig.fromArgs(as.toList)(psetup).provideLayer(osetup)
  //     env  = makeEnv(cfg)
  //     _   <- makeProgram(cfg).provideLayer(env)
  //   } yield ()

  //   program.catchSome { case _: SuccessExitException => ZIO.unit }
  //     .tapError(t => printLineError(s"Error: ${t.getMessage}"))
  //     .ignore
  // }

  // private def makeProgram(cfg: CliConfig): ZIO[Projektor, Throwable, Unit] =
  //   for {
  //     projector <- ZIO.service[Projektor]
  //     _         <- projector.run(cfg.topologyFile)
  //   } yield ()

  // private def makeEnv(cfg: CliConfig): ZLayer[Any, Throwable, Projektor] = {
  //   val parseEnv  = LiveParser.layer
  //   val nameEnv   = ZLayer.succeed(cfg.naming) >>> LiveNamer.layer
  //   val foldEnv   = (ZLayer.succeed(cfg.dot) ++ nameEnv) >>> DotFolder.layer
  //   val encEnv    = nameEnv ++ foldEnv >>> LiveEncoder.layer
  //   val bundleEnv = DotBundler.layer(cfg.isVerbose)

  //   val env = (parseEnv ++ encEnv ++ bundleEnv) >>> LiveProjector.layer

  //   env
  // }

  // private def makeOZEffectSetup(): ZLayer[Any, Nothing, OZEffectSetup] =
  //   StdioEffectSetup.layer

  // private def makePEffectSetup(): OParserSetup =
  //   new DefaultOParserSetup with OParserSetup {
  //     override def errorOnUnknownArgument: Boolean   = false
  //     override def showUsageOnError: Option[Boolean] = Some(false)
  //   }
}
