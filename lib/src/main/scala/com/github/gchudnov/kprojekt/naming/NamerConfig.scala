package com.github.gchudnov.kprojekt.naming

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{ Has, ZLayer }

import scala.annotation.nowarn

final case class NamerConfig(
  maxLenWithoutShortening: Int,
  separator: String
)

object NamerConfig {
  @nowarn
  val live: ZLayer[Any, Nothing, Has[NamerConfig]] =
    ZLayer.succeed(ConfigSource.default.at("naming").loadOrThrow[NamerConfig])
}
