package com.github.gchudnov.kprojekt.formatter

import com.github.gchudnov.kprojekt.formatter.dot.DotConfig
import zio.{ Has, ZIO, ZLayer }

object FolderConfig {
  val layer: ZLayer[Any, Throwable, Has[DotConfig]] =
    ZIO.attempt(DotConfig.load()).toLayer

  def make(space: String): ZLayer[Any, Throwable, Has[DotConfig]] =
    ZIO.attempt(DotConfig.load(space)).toLayer
}
