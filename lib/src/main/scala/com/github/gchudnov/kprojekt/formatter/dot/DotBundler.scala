package com.github.gchudnov.kprojekt.formatter.dot

import com.github.gchudnov.kprojekt.formatter.Bundler
import com.github.gchudnov.kprojekt.util.FileOps
import zio.{ Has, Task, UIO, ZIO, ZLayer }

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.sys.process.{ ProcessLogger, _ }

final class DotBundler() extends Bundler {
  import DotBundler._

  override def bundle(topologyPath: File, data: String): Task[File] =
    for {
      tmpDir          <- ZIO.fromEither(FileOps.createTempDir(DirPrefix))
      _               <- ZIO.logDebug(s"Created temp directory: '${tmpDir.toString}'.")
      fileName         = topologyPath.getName
      dotFile          = FileOps.changeExtension(new File(tmpDir, fileName), ExtDot)
      cylinderFile     = new File(tmpDir, DotConfig.cylinderFileName)
      updData          = data.replaceAll(DotConfig.cylinderFileName, cylinderFile.toString)
      _               <- ZIO.fromEither(FileOps.saveString(dotFile)(updData))
      _               <- ZIO.logDebug(s"Created Dot-file: '${dotFile.toString}'.")
      _               <- ZIO.fromEither(FileOps.saveResource(cylinderFile)(s"images/${DotConfig.cylinderFileName}"))
      _               <- ZIO.logDebug(s"Created Cylinder-file: '${cylinderFile.toString}'.")
      (procLogger, qs) = buildProcessLogger()
      pngFile          = FileOps.changeExtension(topologyPath, ExtPng)
      _               <- ZIO.logDebug(s"Producing PNG: '${pngFile.toString}'.")
      _                = s"dot -Tpng ${dotFile.getAbsolutePath} -o${pngFile.getAbsolutePath}" ! procLogger
      logs             = qs.toList
      _               <- ZIO.foreachDiscard(logs)(identity)
    } yield pngFile

  private def buildProcessLogger(): (ProcessLogger, ListBuffer[UIO[Unit]]) = {
    val qs = ListBuffer.empty[UIO[Unit]]
    val procLogger = ProcessLogger(
      str => qs.addOne(ZIO.logDebug(str)),
      err => qs.addOne(ZIO.logDebug(err))
    )
    (procLogger, qs)
  }
}

object DotBundler {
  private val DirPrefix: String = "dot-bundle"
  private val ExtDot: String    = "dot"
  private val ExtPng: String    = "png"

  def layer: ZLayer[Any, Nothing, Has[Bundler]] =
    ZIO.succeed(new DotBundler()).toLayer
}
