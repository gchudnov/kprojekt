package com.github.gchudnov.kprojekt.naming

final case class NamerConfig(
  maxLenWithoutShortening: Int,
  separator: String
)

object NamerConfig {

  def default: NamerConfig =
    NamerConfig(
      maxLenWithoutShortening = 13,
      separator = ","
    )
}
