package com.github.gchudnov.kprojekt

import java.nio.file.Path
import scopt.OParser

case class AppConfig(foo: Int = -1)

/**
  * Command-Line Application for topology parser
  *
  * bloop run cli -m com.github.gchudnov.kprojekt.Cli
  */
object Cli extends App {

  val p = getClass.getPackage
  println(p.getName)

  val builder = OParser.builder[AppConfig]
  val parser = {
    import builder._
    OParser.sequence(
      programName("kprojekt-cli"),
      head("kprojekt-cli", "4.x"),
      opt[Int]('f', "foo")
        .action((x, c) => c.copy(foo = x))
        .text("foo is an integer property")
    )
  }

  OParser.parse(parser, args, AppConfig()) match {
    case Some(config) =>
    // do something
    case _ =>
    // arguments are bad, error message will have been displayed
  }

}
