package com.github.gchudnov.kprojekt.formatter

import java.io.File

import com.github.gchudnov.kprojekt.formatter.dot.DotBundler
import zio.logging.{ Logger, Logging }
import zio.{ Has, RIO, Task, ZIO, ZLayer }

object Bundler {
  type Bundler = Has[Bundler.Service]

  trait Service {
    def bundle(topologyPath: File, data: String): Task[File]
  }

  val any: ZLayer[Bundler, Nothing, Bundler] =
    ZLayer.requires[Bundler]

  val live: ZLayer[Logging, Nothing, Bundler] = ZLayer.fromService { (logging: Logger[String]) =>
    new DotBundler(logging)
  }

  def bundle(topologyPath: File, data: String): RIO[Bundler, File] = ZIO.accessM(_.get.bundle(topologyPath, data))
}
