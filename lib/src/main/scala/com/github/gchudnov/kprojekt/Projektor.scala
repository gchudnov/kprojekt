package com.github.gchudnov.kprojekt

import zio._

import java.io.File

trait Projektor {
  def run(topologyFile: File): Task[File]
}

object Projektor {
  def run(topologyFile: File): RIO[Projektor, File] =
    ZIO.serviceWithZIO(_.run(topologyFile))
}
