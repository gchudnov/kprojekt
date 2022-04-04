package com.github.gchudnov.kprojekt.formatter.dot

import com.github.gchudnov.kprojekt.formatter.Bundler
import com.github.gchudnov.kprojekt.util.FileOps
import zio._

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.sys.process.{ ProcessLogger, _ }

final class DotBundler(isVerbose: Boolean = false) extends Bundler {
  import DotBundler._

  override def bundle(topologyPath: File, data: String): Task[File] =
    for {
      tmpDir          <- ZIO.fromEither(FileOps.createTempDir(DirPrefix))
      _               <- ZIO.logInfo(s"Created temp directory: '${tmpDir.toString}'.").when(isVerbose)
      fileName         = topologyPath.getName
      dotFile          = FileOps.changeExtension(new File(tmpDir, fileName), ExtDot)
      cylinderFile     = new File(tmpDir, DotConfig.cylinderFileName)
      updData          = data.replaceAll(DotConfig.cylinderFileName, cylinderFile.toString)
      _               <- ZIO.fromEither(FileOps.saveString(dotFile)(updData))
      _               <- ZIO.logInfo(s"Created Dot-file: '${dotFile.toString}'.").when(isVerbose)
      _               <- ZIO.fromEither(FileOps.saveResource(cylinderFile)(s"images/${DotConfig.cylinderFileName}"))
      _               <- ZIO.logInfo(s"Created Cylinder-file: '${cylinderFile.toString}'.").when(isVerbose)
      (procLogger, qs) = buildProcessLogger()
      pngFile          = FileOps.changeExtension(topologyPath, ExtPng)
      _               <- ZIO.logInfo(s"Producing PNG: '${pngFile.toString}'.").when(isVerbose)
      _                = s"dot -Tpng ${dotFile.getAbsolutePath} -o${pngFile.getAbsolutePath}" ! procLogger
      logs             = qs.toList
      _               <- ZIO.foreachDiscard(logs)(it => ZIO.logInfo(it).when(isVerbose))
    } yield pngFile

  private def buildProcessLogger(): (ProcessLogger, ListBuffer[String]) = {
    val qs = ListBuffer.empty[String]
    val procLogger = ProcessLogger(
      str => qs.addOne(s"stdout: ${str}"),
      err => qs.addOne(s"stderr: ${err}")
    )
    (procLogger, qs)
  }
}

object DotBundler {
  private val DirPrefix: String = "dot-bundle"
  private val ExtDot: String    = "dot"
  private val ExtPng: String    = "png"

  def layer(isVerbose: Boolean): ZLayer[Any, Nothing, Bundler] =
    ZIO.succeed(new DotBundler(isVerbose)).toLayer
}
