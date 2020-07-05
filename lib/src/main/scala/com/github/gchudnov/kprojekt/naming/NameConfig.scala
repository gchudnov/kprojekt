package com.github.gchudnov.kprojekt.naming

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.ZLayer

import scala.annotation.nowarn

final case class NameConfig(
  maxLenWithoutShortening: Int,
  separator: String
)

object NameConfig {
  @nowarn
  val live: ZLayer[Any, Nothing, NameConfig] =
    ZLayer.succeedMany(ConfigSource.default.at("naming").loadOrThrow[NameConfig])
}
