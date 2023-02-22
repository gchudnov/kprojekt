package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.{ BuildInfo => KBuildInfo }
import zio.cli.HelpDoc.Span.text
import zio.cli._
import java.nio.file.Path
import com.github.gchudnov.kprojekt.internal.DotProcessor

object Cli extends ZIOCliDefault {

  val verboseFlag: Options[Boolean] = Options.boolean("verbose").alias("v")
  val versionFlag: Options[Boolean] = Options.boolean("version")

  val options = (verboseFlag ++ versionFlag).as(CliOptions)

  val inputTopologyArgs: Args[Path] = Args.file("input-topology", Exists.Yes)

  val args = inputTopologyArgs

  val rootCommand = Command(KBuildInfo.name, options, args)

  val cliApp = CliApp.make(
    name = "KProjekt Cli",
    version = KBuildInfo.version,
    summary = text("Visualize Kafka Topology"),
    command = rootCommand
  ) { case (options, path) =>
    DotProcessor.toDot(options, path)
  }

  override def run =
    super.run.ignore

}
