package com.github.gchudnov.kprojekt.formatter.dot

final case class DotConfig(indent: Int, fontName: String, fontSize: Int, isEmbedStore: Boolean)

object DotConfig {
  val CylinderFileName: String = "cylinder.png"
}
