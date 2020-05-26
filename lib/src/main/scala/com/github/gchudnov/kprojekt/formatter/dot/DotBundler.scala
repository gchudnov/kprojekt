package com.github.gchudnov.kprojekt.formatter.dot

import java.io.File

import com.github.gchudnov.kprojekt.formatter.Bundler
import com.github.gchudnov.kprojekt.util.FileOps
import zio.logging.Logger
import zio.{ Task, ZIO }

import scala.sys.process.{ ProcessLogger, _ }

final class DotBundler(logger: Logger[String]) extends Bundler.Service {
  import DotBundler._

  override def bundle(topologyPath: File, data: String): Task[File] =
    for {
      tmpDir      <- ZIO.fromEither(FileOps.createTempDir(DirPrefix))
      _           <- logger.debug(s"Created temp directory: '${tmpDir.toString}'.")
      fileName     = topologyPath.getName
      dotFile      = FileOps.changeExtension(new File(tmpDir, fileName), ExtDot)
      cylinderFile = new File(tmpDir, DotConfig.cylinderFileName)
      updData      = data.replaceAll(DotConfig.cylinderFileName, cylinderFile.toString)
      _           <- ZIO.fromEither(FileOps.saveString(dotFile)(updData))
      _           <- logger.debug(s"Created Dot-file: '${dotFile.toString}'.")
      _           <- ZIO.fromEither(FileOps.saveResource(cylinderFile)(s"images/${DotConfig.cylinderFileName}"))
      _           <- logger.debug(s"Created Cylinder-file: '${cylinderFile.toString}'.")
      procLogger   = buildProcessLogger()
      pngFile      = FileOps.changeExtension(topologyPath, ExtPng)
      _           <- logger.debug(s"Producing PNG: '${pngFile.toString}'.")
      _            = s"dot -Tpng -v ${dotFile.getAbsolutePath} -o${pngFile.getAbsolutePath}" ! procLogger
    } yield pngFile

  private def buildProcessLogger(): ProcessLogger =
    ProcessLogger(
      str => logger.debug(str),
      err => logger.error(err)
    )
}

object DotBundler {
  private val DirPrefix: String = "dot-bundle"
  private val ExtDot: String    = "dot"
  private val ExtPng: String    = "png"
}
