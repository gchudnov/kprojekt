package com.github.gchudnov.kprojekt.transform

import com.github.gchudnov.kprojekt.format.Dot
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.Projektor
import com.github.gchudnov.kprojekt.util.FileOps
import java.io.File
import scala.util.Either
import sys.process._

case class TransformConfig(topologyFile: File)

object Transformer {

  private val tmpPrefix: String = "res"
  private val cylinderFileName = "cylinder.png"

  private val logger = ProcessLogger(out => {}, err => {
    if (!err.contains("size too small for label")) {
      Console.err.println(err)
    }
  })

  def run(config: TransformConfig): Either[Throwable, Unit] = {
    val name = config.topologyFile.getName()

    FileOps
      .stringFromFile(config.topologyFile)
      .flatMap(Parser.run)
      .flatMap(desc => {
        FileOps
          .createTempDir(tmpPrefix)
          .flatMap(tmpDir => {
            import com.github.gchudnov.kprojekt.format.DotInstances._
            val data = Projektor.run[Dot](name, desc)

            val dotFile: File = FileOps.changeExtension(new File(tmpDir, name), "dot")
            FileOps
              .saveString(dotFile)(data)
              .flatMap(_ => {
                val cylinderFile = new File(tmpDir, cylinderFileName)
                FileOps.saveResource(cylinderFile)(s"images/${cylinderFileName}")
              })
              .map(_ => {
                val pngFile = FileOps.changeExtension(config.topologyFile, "png")
                s"dot -Tpng ${dotFile.getAbsolutePath()} -o${pngFile.getAbsolutePath}" ! (logger)
              })
          })
      })
  }

}
