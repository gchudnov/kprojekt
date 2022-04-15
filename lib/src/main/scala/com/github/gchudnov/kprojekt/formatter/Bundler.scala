package com.github.gchudnov.kprojekt.formatter

import zio._

import java.io.File

trait Bundler {
  def bundle(topologyPath: File, data: String): Task[File]
}

object Bundler {
  def bundle(topologyPath: File, data: String): RIO[Bundler, File] =
    ZIO.serviceWithZIO(_.bundle(topologyPath, data))
}
