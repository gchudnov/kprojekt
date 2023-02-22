package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.{ BuildInfo => KBuildInfo }
import zio.cli.HelpDoc.Span.text
import zio.Console._
import zio._
import zio.logging._
import zio.cli._
import java.nio.file.Path

object Cli extends ZIOCliDefault {

  case class KOptions(verbose: Boolean, version: Boolean)

  val verboseFlag: Options[Boolean] = Options.boolean("verbose").alias("v")
  val versionFlag: Options[Boolean] = Options.boolean("version")

  val options = (verboseFlag ++ versionFlag).as(KOptions)

  val inputTopologyArgs: Args[Path] = Args.file("input-topology", Exists.Yes)

  val args = inputTopologyArgs

  val rootCommand = Command(KBuildInfo.name, options, args)

  val cliApp = CliApp.make(
    name = "KProjekt Cli",
    version = KBuildInfo.version,
    summary = text("Visualize Kafka Topology"),
    command = rootCommand
  ) { case _ =>
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

/*
final class DotBundler(isVerbose: Boolean = false) extends Bundler {
  import DotBundler._

  override def bundle(topologyPath: File, data: String): Task[File] =
    for {
      tmpDir          <- ZIO.fromEither(Dirs.makeTempDir(DirPrefix))
      _               <- ZIO.logInfo(s"Created temp directory: '${tmpDir.toString}'.").when(isVerbose)
      fileName         = topologyPath.getName
      dotFile          = Files.changeExtension(new File(tmpDir, fileName), ExtDot)
      cylinderFile     = new File(tmpDir, DotConfig.cylinderFileName)
      updData          = data.replaceAll(DotConfig.cylinderFileName, cylinderFile.toString)
      _               <- ZIO.fromEither(Files.saveString(dotFile, updData))
      _               <- ZIO.logInfo(s"Created Dot-file: '${dotFile.toString}'.").when(isVerbose)
      _               <- ZIO.fromEither(Resources.saveResource(cylinderFile, s"images/${DotConfig.cylinderFileName}"))
      _               <- ZIO.logInfo(s"Created Cylinder-file: '${cylinderFile.toString}'.").when(isVerbose)
      (procLogger, qs) = buildProcessLogger()
      pngFile          = Files.changeExtension(topologyPath, ExtPng)
      _               <- ZIO.logInfo(s"Producing PNG: '${pngFile.toString}'.").when(isVerbose)
      _                = s"dot -Tpng ${dotFile.getAbsolutePath} -o${pngFile.getAbsolutePath}" ! procLogger
      logs             = qs.toList
      _               <- ZIO.foreachDiscard(logs)(it => ZIO.logInfo(it).when(isVerbose))
    } yield pngFile

  private def buildProcessLogger(): (ProcessLogger, ListBuffer[String]) = {
    val qs = ListBuffer.empty[String]
    val procLogger = ProcessLogger(
      str => qs.addOne(s"stdout: ${str}"),
      err => qs.addOne(s"stderr: ${err}")
    )
    (procLogger, qs)
  }
}

object DotBundler {
  private val DirPrefix: String = "dot-bundle"
  private val ExtDot: String    = "dot"
  private val ExtPng: String    = "png"

  def layer(isVerbose: Boolean): ZLayer[Any, Nothing, Bundler] =
    ZLayer(ZIO.succeed(new DotBundler(isVerbose)))
}


 */
