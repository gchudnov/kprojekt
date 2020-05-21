package com.github.gchudnov.kprojekt

import java.io.File

import com.github.gchudnov.kprojekt.process.{ Processor }
import scopt.OParser

/**
 * Command-Line Application for topology parser
 *
 * building an image:
 * sbt 'show graalvm-native-image:packageBin'
 * OR
 * sbt cli/assembly
 *
 * bloop run cli -m com.github.gchudnov.kprojekt.Cli
 * bloop run cli -m com.github.gchudnov.kprojekt.Cli -- /path/to/toplogogy.log
 */
object Cli extends App {

  final case class AppConfig(topologyFile: File = null, isVerbose: Boolean = false)

  val builder = OParser.builder[AppConfig]
  val parser = {
    import builder._
    OParser.sequence(
      programName(BuildInfo.name),
      head(BuildInfo.name, BuildInfo.version),
      help("help").text("prints this usage text"),
      opt[Unit]("verbose")
        .action((_, c) => c.copy(isVerbose = true))
        .text("verbose mode"),
      arg[File]("<file>")
        .required()
        .action((x, c) => c.copy(topologyFile = x))
        .text("path to the topology description"),
      version("version")
    )
  }

  OParser.parse(parser, args, AppConfig()) match {
    case Some(config) =>
      import com.github.gchudnov.kprojekt.formatter.dot.DotInstances._
      Processor.run(config.isVerbose, config.topologyFile) match {
        case Right(_) =>
        // no-op
        case Left(ex) =>
          Console.err.println(ex.getMessage)
          sys.exit(-1)
      }
    case _ =>
    // no-op
  }

}
