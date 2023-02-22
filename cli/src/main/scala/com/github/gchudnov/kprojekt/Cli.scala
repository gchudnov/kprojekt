package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.{ BuildInfo => KBuildInfo }
import com.github.gchudnov.kprojekt.input.Parser
import com.github.gchudnov.kprojekt.output.Builder
import com.github.gchudnov.kprojekt.output.Writer
import zio.cli.HelpDoc.Span.text
import zio.Console._
import zio._
import zio.cli._
import java.nio.file.Path
import com.github.gchudnov.kprojekt.util.Files

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
  ) { case (options, path) =>
      toDot(options, path)
  }

  private def toDot(options: KOptions, path: Path) = {
    val env = makeDotEnv
    val dotFile = Files.changeExtension(path.toFile(), "dot")

    val program = for {
      input    <- ZIO.fromEither(Files.lines(path.toFile()))
      desc     <- Parser.parse(input)
      dotStr   <- Writer.write("topo", desc)
      _        <- ZIO.fromEither(Files.save(dotFile, dotStr))
    } yield ()

    program.provideLayer(env)
  }

  private def makeDotEnv: ULayer[Parser with Writer] = {
    val parserEnv = Parser.make
    val writerEnv = Builder.dot >>> Writer.make

    val env = parserEnv ++ writerEnv

    env    
  }
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
