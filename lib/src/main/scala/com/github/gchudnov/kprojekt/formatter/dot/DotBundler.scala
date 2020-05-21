package com.github.gchudnov.kprojekt.formatter.dot

import java.io.File

import com.github.gchudnov.kprojekt.formatter.Bundler
import com.github.gchudnov.kprojekt.util.FileOps

import scala.sys.process.ProcessLogger
import scala.sys.process._

final case class DotBundler() extends Bundler[Dot] {

  private val dirPrefix: String = "dot-bundle"

  override def bundle(isVerbose: Boolean, logger: ProcessLogger, topologyPath: File, data: String): Either[Throwable, File] =
    FileOps
      .createTempDir(dirPrefix)
      .flatMap { tmpDir =>
        if (isVerbose)
          logger.out(s"Temp directory: ${tmpDir.toString} is created.")

        val fileName           = topologyPath.getName
        val dotFile: File      = FileOps.changeExtension(new File(tmpDir, fileName), "dot")
        val cylinderFile: File = new File(tmpDir, DotConfig.CylinderFileName)

        val updData = data.replaceAll(DotConfig.CylinderFileName, cylinderFile.toString)

        FileOps
          .saveString(dotFile)(updData)
          .flatMap { _ =>
            if (isVerbose)
              logger.out(s"Dot-file: ${dotFile.toString} is created.")

            FileOps.saveResource(cylinderFile)(s"images/${DotConfig.CylinderFileName}")
          }
          .map { _ =>
            if (isVerbose)
              logger.out(s"Cylinder-file: ${cylinderFile.toString} is created.")

            val pngFile = FileOps.changeExtension(topologyPath, "png")
            s"dot -Tpng -v ${dotFile.getAbsolutePath} -o${pngFile.getAbsolutePath}" ! (logger)

            pngFile
          }
      }
}
