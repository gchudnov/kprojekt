package com.github.gchudnov.kprojekt.formatter

import com.github.gchudnov.kprojekt.formatter.dot.DotConfig
import zio.ZLayer

object FolderConfig {
  val live: ZLayer[Any, Nothing, DotConfig] =
    ZLayer.succeedMany(DotConfig.load())

  def make(space: String): ZLayer[Any, Nothing, DotConfig] =
    ZLayer.succeedMany(DotConfig.load(space))
}
