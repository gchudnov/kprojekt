package com.github.gchudnov.kprojekt.naming

import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._
import zio.ZLayer

final case class NameConfig(
  maxLenWithoutShortening: Int,
  separator: String
)

object NameConfig {
  val live: ZLayer[Any, Nothing, NameConfig] =
    ZLayer.succeedMany(ConfigSource.default.at("naming").loadOrThrow[NameConfig])
}
