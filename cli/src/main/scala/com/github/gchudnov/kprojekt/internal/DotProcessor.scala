package com.github.gchudnov.kprojekt.internal

import com.github.gchudnov.kprojekt.CliOptions
import com.github.gchudnov.kprojekt.input.Parser
import com.github.gchudnov.kprojekt.output.Builder
import com.github.gchudnov.kprojekt.output.Writer
import com.github.gchudnov.kprojekt.util.Files
import java.nio.file.Path
import zio._
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.sys.process.{ ProcessLogger, _ }

object DotProcessor {

  def toDot(options: CliOptions, path: Path) = {
    val env     = makeDotEnv
    val dotFile = Files.changeExtension(path.toFile(), "dot")

    val program = for {
      input  <- ZIO.fromEither(Files.lines(path.toFile()))
      desc   <- Parser.parse(input)
      dotStr <- Writer.write("topo", desc)
      _      <- ZIO.fromEither(Files.save(dotFile, dotStr))
      _      <- toPng(dotFile, options.verbose)
    } yield ()

    program.provideLayer(env)
  }

  private def toPng(dotFile: File, isVerbose: Boolean): UIO[Unit] = {
    val (procLogger, qs) = buildProcessLogger()

    val pngFile = Files.changeExtension(dotFile, "png")

    s"dot -Tpng ${dotFile.getAbsolutePath} -o${pngFile.getAbsolutePath}" ! procLogger

    val logs = qs.toList
    ZIO
      .foreachDiscard(logs)({ it =>
        if (it.startsWith("stdout")) {
          ZIO.logInfo(it)
        } else {
          ZIO.logError(it)
        }
      })
      .when(isVerbose)
      .unit
  }

  private def buildProcessLogger(): (ProcessLogger, ListBuffer[String]) = {
    val qs = ListBuffer.empty[String]
    val procLogger = ProcessLogger(
      str => qs.addOne(s"stdout: ${str}"),
      err => qs.addOne(s"stderr: ${err}")
    )
    (procLogger, qs)
  }

  private def makeDotEnv: ULayer[Parser with Writer] = {
    val parserEnv = Parser.make
    val writerEnv = Builder.dot >>> Writer.make

    val env = parserEnv ++ writerEnv

    env
  }

}
