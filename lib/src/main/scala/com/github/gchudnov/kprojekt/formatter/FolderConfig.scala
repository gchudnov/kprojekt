package com.github.gchudnov.kprojekt.formatter

import com.github.gchudnov.kprojekt.formatter.dot.DotConfig
import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._
import zio.ZLayer

object FolderConfig {
  val live: ZLayer[Any, Nothing, DotConfig] =
    ZLayer.succeedMany(ConfigSource.default.at("formatters.dot").loadOrThrow[DotConfig])
}
