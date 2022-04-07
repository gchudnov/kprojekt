package com.github.gchudnov.kprojekt.formatter.dot

import com.github.gchudnov.kprojekt.formatter.FolderConfig
import pureconfig.generic.auto._

final case class DotConfig(
  indent: Int,
  fontName: String,
  fontSize: Int,
  isEmbedStore: Boolean,
  hasLegend: Boolean,
  space: DotSpace
) extends FolderConfig

object DotConfig {
  val cylinderFileName: String = "cylinder.png"

  def default: DotConfig =
    DotConfig(
      indent = 2,
      fontName = "sans-serif",
      fontSize = 10,
      isEmbedStore = false,
      hasLegend = true,
      space = DotSpace.Medium
    )
}
