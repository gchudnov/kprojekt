package com.github.gchudnov.kprojekt.formatter.dot

import pureconfig.{ ConfigSource, _ }
import pureconfig.error.CannotConvert
import pureconfig.generic.auto._

final case class DotConfig(
  indent: Int,
  fontName: String,
  fontSize: Int,
  isEmbedStore: Boolean,
  hasLegend: Boolean,
  space: DotSpace
)

object DotConfig {
  val cylinderFileName: String = "cylinder.png"

  private implicit val spaceReader: ConfigReader[DotSpace] = ConfigReader[String]
    .emap(it =>
      DotSpace
        .parse(it)
        .fold(t => Left(CannotConvert("space", "space", t.getMessage)), value => Right(value))
    )

  def load(space: String = ""): DotConfig = {
    val str = makeStringSource(space)

    val userSource    = ConfigSource.string(str)
    val defaultSource = ConfigSource.default

    userSource.withFallback(defaultSource).at("formatters.dot").loadOrThrow[DotConfig]
  }

  private def makeStringSource(space: String): String =
    if (space.nonEmpty)
      s"""
         |formatters {
         |  dot {
         |    space: "${space}"
         |  }
         |}
         |""".stripMargin
    else
      ""
}
