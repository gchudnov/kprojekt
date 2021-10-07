package com.github.gchudnov.kprojekt.naming

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{Has, ZIO, ZLayer}

import scala.annotation.nowarn

final case class NamerConfig(
  maxLenWithoutShortening: Int,
  separator: String
)

object NamerConfig {
  @nowarn
  val layer: ZLayer[Any, Throwable, Has[NamerConfig]] =
    ZIO.attempt(ConfigSource.default.at("naming").loadOrThrow[NamerConfig]).toLayer
}
