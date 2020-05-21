package com.github.gchudnov.kprojekt.process

import java.io.File

import com.github.gchudnov.kprojekt.Projektor
import com.github.gchudnov.kprojekt.formatter.{ Bundler, Encoder, Tag }
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.util.FileOps

import scala.sys.process._
import scala.util.Either

object Processor {

  private def buildLogger(isVerbose: Boolean): ProcessLogger =
    ProcessLogger(
      str =>
        if (isVerbose)
          Console.out.println(str),
      err =>
        if (!err.contains("size too small for label") || isVerbose)
          Console.err.println(err)
    )

  def run[T <: Tag: Encoder: Bundler](isVerbose: Boolean = false, topologyFile: File = null): Either[Throwable, File] = {
    val logger = buildLogger(isVerbose)

    val name = topologyFile.getName

    FileOps
      .stringFromFile(topologyFile)
      .flatMap(Parser.run)
      .flatMap { desc =>
        val data = Projektor.encode[T](name, desc)
        val file = Projektor.bundle[T](isVerbose, logger, topologyFile, data)
        file
      }
  }

}
