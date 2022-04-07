package com.github.gchudnov.kprojekt.naming

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio._

import scala.annotation.nowarn

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
