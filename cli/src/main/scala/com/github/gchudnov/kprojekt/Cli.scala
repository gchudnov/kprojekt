package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.BuildInfo
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import scopt.OParser

case class AppConfig(topologyPath: Path = null)

/**
  * Command-Line Application for topology parser
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
        .action((x, c) => c.copy(topologyPath = Paths.get(x.toURI())))
        .text("path to the topology description")
    )
  }

  OParser.parse(parser, args, AppConfig()) match {
    case Some(config) =>
      println(config)
    case _ =>
      println("cannot parse config")
  }

}
