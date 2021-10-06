package com.github.gchudnov.kprojekt.formatter

import zio.{ Has, RIO, Task, ZIO }

import java.io.File

trait Bundler {
  def bundle(topologyPath: File, data: String): Task[File]
}

object Bundler {
  def bundle(topologyPath: File, data: String): RIO[Has[Bundler], File] =
    ZIO.serviceWith(_.bundle(topologyPath, data))
}
