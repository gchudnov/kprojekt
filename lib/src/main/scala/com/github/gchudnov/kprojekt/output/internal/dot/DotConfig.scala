package com.github.gchudnov.kprojekt.output.internal.dot

/**
 * DotBuilder Configuration
 */
final case class DotConfig(
  fontName: String,
  fontSize: Int,
  indent: Int
)

object DotConfig {

  def default: DotConfig =
    DotConfig(
      fontName = "sans-serif",
      fontSize = 10,
      indent = 2
    )

}
