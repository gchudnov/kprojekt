package com.github.gchudnov.kprojekt.formatter

import com.github.gchudnov.kprojekt.formatter.dot.DotConfig
import zio.{ Has, ZLayer }

object FolderConfig {
  val live: ZLayer[Any, Nothing, Has[DotConfig]] =
    ZLayer.succeed(DotConfig.load())

  def make(space: String): ZLayer[Any, Nothing, Has[DotConfig]] =
    ZLayer.succeed(DotConfig.load(space))
}
