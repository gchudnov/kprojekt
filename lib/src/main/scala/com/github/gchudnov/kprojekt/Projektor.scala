package com.github.gchudnov.kprojekt

import zio.{ Has, Task, ZIO }

import java.io.File

trait Projektor {
  def run(topologyFile: File): Task[File]
}

object Projektor {
  def run(topologyFile: File): ZIO[Has[Projektor], Throwable, File] =
    ZIO.serviceWith(_.run(topologyFile))

}
