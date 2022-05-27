package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.encoder.LiveEncoder
import com.github.gchudnov.kprojekt.formatter.dot.{ DotBundler, DotFolder }
import com.github.gchudnov.kprojekt.naming.LiveNamer
import com.github.gchudnov.kprojekt.parser.LiveParser
import com.github.gchudnov.kprojekt.zopt.SuccessExitException
import com.github.gchudnov.kprojekt.zopt.ozeffectsetup.{ OZEffectSetup, StdioEffectSetup }
import scopt.{ DefaultOParserSetup, OParserSetup }
import zio.Console._
import zio._
import zio.logging.backend.SLF4J
import zio.logging.LogFormat

object Cli extends ZIOAppDefault {

  private val slf4jAspect = SLF4J.slf4j(
    logLevel = LogLevel.All,
    format = LogFormat.line
  )

  override val bootstrap: ZLayer[ZIOAppArgs with Scope, Any, Environment] = ZLayer.empty ++ slf4jAspect

  override def run: ZIO[ZIOAppArgs, Any, Any] = {
    val osetup: ZLayer[Any, Throwable, OZEffectSetup] = makeOZEffectSetup()
    val psetup: OParserSetup                          = makePEffectSetup()

    val program = for {
      as  <- getArgs
      cfg <- CliConfig.fromArgs(as.toList)(psetup).provideLayer(osetup)
      env  = makeEnv(cfg)
      _   <- makeProgram(cfg).provideLayer(env)
    } yield ()

    program.catchSome { case _: SuccessExitException => ZIO.unit }
      .tapError(t => printLineError(s"Error: ${t.getMessage}"))
      .ignore

    // ZIO.withRuntimeConfig(RuntimeConfig.default.copy(logger = ZLogger.none) @@ slf4jAspect)(logic)
  }

  private def makeProgram(cfg: CliConfig): ZIO[Projektor, Throwable, Unit] =
    for {
      projector <- ZIO.service[Projektor]
      _         <- projector.run(cfg.topologyFile)
    } yield ()

  private def makeEnv(cfg: CliConfig): ZLayer[Any, Throwable, Projektor] = {
    val parseEnv  = LiveParser.layer
    val nameEnv   = ZLayer.succeed(cfg.naming) >>> LiveNamer.layer
    val foldEnv   = (ZLayer.succeed(cfg.dot) ++ nameEnv) >>> DotFolder.layer
    val encEnv    = nameEnv ++ foldEnv >>> LiveEncoder.layer
    val bundleEnv = DotBundler.layer(cfg.isVerbose)

    val env = (parseEnv ++ encEnv ++ bundleEnv) >>> LiveProjector.layer

    env
  }

  private def makeOZEffectSetup(): ZLayer[Any, Nothing, OZEffectSetup] =
    StdioEffectSetup.layer

  private def makePEffectSetup(): OParserSetup =
    new DefaultOParserSetup with OParserSetup {
      override def errorOnUnknownArgument: Boolean   = false
      override def showUsageOnError: Option[Boolean] = Some(false)
    }
}
