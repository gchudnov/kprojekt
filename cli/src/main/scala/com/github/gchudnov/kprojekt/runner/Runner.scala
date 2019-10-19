package com.github.gchudnov.kprojekt.runner

import cats.implicits._
import java.io.File
import scala.io.Source
import scala.util.{Using, Either}
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.Projektor
import com.github.gchudnov.kprojekt.format.Dot

case class RunnerConfig(topologyFile: File)

object Runner {

  def run(config: RunnerConfig): Either[Throwable, Unit] = {
    Either
      .catchNonFatal(Using.resource(Source.fromFile(config.topologyFile)) { file =>
        file.getLines.mkString
      })
      .flatMap(Parser.run)
      .map(desc => {
        import com.github.gchudnov.kprojekt.format.DotInstances._

        val name = config.topologyFile.getName()
        val output = Projektor.run[Dot](name, desc)
        print(output)
      })
  }

}
