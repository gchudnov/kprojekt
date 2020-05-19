package com.github.gchudnov.kprojekt.transform

import com.github.gchudnov.kprojekt.format.Dot
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.Projektor
import com.github.gchudnov.kprojekt.util.FileOps
import java.io.File
import scala.util.Either
import sys.process._

case class TransformConfig(topologyFile: File, isVerbose: Boolean)

object Transformer {

  private val tmpPrefix: String = "res"
  private val cylinderFileName  = "cylinder.png"

  private def buildLogger(isVerbose: Boolean) =
    ProcessLogger(
      str =>
        if (isVerbose)
          Console.out.println(str),
      err =>
        if (!err.contains("size too small for label") || isVerbose)
          Console.err.println(err)
    )

  def run(config: TransformConfig): Either[Throwable, Unit] = {
    val name      = config.topologyFile.getName
    val isVerbose = config.isVerbose

    val logger = buildLogger(isVerbose)

    FileOps
      .stringFromFile(config.topologyFile)
      .flatMap(Parser.run)
      .flatMap { desc =>
        FileOps
          .createTempDir(tmpPrefix)
          .flatMap { tmpDir =>
            if (isVerbose)
              logger.out(s"Temp directory: ${tmpDir.toString} is created.")

            import com.github.gchudnov.kprojekt.format.DotInstances._
            val data = Projektor.run[Dot](name, desc)

            val dotFile: File = FileOps.changeExtension(new File(tmpDir, name), "dot")
            val cylinderFile  = new File(tmpDir, cylinderFileName)

            val updData = data.replaceAll(cylinderFileName, cylinderFile.toString)

            FileOps
              .saveString(dotFile)(updData)
              .flatMap { _ =>
                if (isVerbose)
                  logger.out(s"Dot-file: ${dotFile.toString} is created.")

                FileOps.saveResource(cylinderFile)(s"images/${cylinderFileName}")
              }
              .map { _ =>
                if (isVerbose)
                  logger.out(s"Cylinder-file: ${cylinderFile.toString()} is created.")

                val pngFile = FileOps.changeExtension(config.topologyFile, "png")
                s"dot -Tpng -v ${dotFile.getAbsolutePath} -o${pngFile.getAbsolutePath}" ! (logger)
              }
          }
      }
  }

}
