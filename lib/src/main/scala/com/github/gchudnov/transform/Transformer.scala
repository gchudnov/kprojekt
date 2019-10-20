package com.github.gchudnov.kprojekt.transform

import cats.implicits._
import com.github.gchudnov.kprojekt.format.Dot
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.Projektor
import com.github.gchudnov.kprojekt.util.FileOps
import java.io.File
import scala.util.Either

case class TransformConfig(topologyFile: File)

object Transformer {

  def run(config: TransformConfig): Either[Throwable, Unit] = {
    Either
      .catchNonFatal(FileOps.stringFromFile(config.topologyFile))
      .flatMap(Parser.run)
      .map(desc => {
        import com.github.gchudnov.kprojekt.format.DotInstances._

        val name = config.topologyFile.getName()
        val output = Projektor.run[Dot](name, desc)
        print(output)
      })
  }

}
