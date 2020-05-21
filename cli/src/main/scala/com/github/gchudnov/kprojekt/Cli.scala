package com.github.gchudnov.kprojekt

import java.io.File

import com.github.gchudnov.kprojekt.process.Processor
import scopt.{ OParser, OParserBuilder }
import zio.ZIO

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
object Cli extends zio.App {

  final case class AppConfig(topologyFile: File = new File("."), isVerbose: Boolean = false)

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
      arg[File]("<file>")
        .required()
        .action((x, c) => c.copy(topologyFile = x))
        .text("path to topology description"),
      version("version")
    )
  }

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    import com.github.gchudnov.kprojekt.formatter.dot.DotInstances._

    val program = for {
      config <- ZIO.fromOption(OParser.parse(parser, args, AppConfig()))
      _      <- ZIO.fromEither(Processor.run(config.isVerbose, config.topologyFile))
    } yield ()

    program.fold(_ => 1, _ => 0)
  }
}
