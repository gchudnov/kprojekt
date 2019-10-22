package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.BuildInfo
import com.github.gchudnov.kprojekt.transform.{Transformer, TransformConfig}
import java.io.File
import scopt.OParser

final case class AppConfig(topologyFile: File = null) {
  def toTransformConfig: TransformConfig = TransformConfig(topologyFile)
}

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

  val builder = OParser.builder[AppConfig]
  val parser = {
    import builder._
    OParser.sequence(
      programName(BuildInfo.name),
      head(BuildInfo.name, BuildInfo.version),
      help("help").text("prints this usage text"),
      arg[File]("<file>")
        .required()
        .action((x, c) => c.copy(topologyFile = x))
        .text("path to the topology description"),
      version("version")
    )
  }

  OParser.parse(parser, args, AppConfig()) match {
    case Some(config) =>
      Transformer.run(config.toTransformConfig) match {
        case Right(_) =>
        // no-op
        case Left(ex) =>
          Console.err.println(ex.getMessage())
          sys.exit(-1)
      }
    case _ =>
    // no-op
  }

}
