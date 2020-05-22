package com.github.gchudnov.kprojekt.formatter.dot

final case class DotConfig(ident: Int, fontName: String, fontSize: Int, isEmbedStore: Boolean)

object DotConfig {
  val CylinderFileName: String = "cylinder.png"
}
