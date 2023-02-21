package com.github.gchudnov.kprojekt.util

import java.io.{ BufferedWriter, File, FileOutputStream, FileWriter }
import java.nio.file.{Files => JFiles}

import scala.io.Source
import scala.util.Using
import scala.util.control.Exception._

object Files {

  def linesFromFile(file: File): Either[Throwable, String] =
    nonFatalCatch.either {
      Using.resource(Source.fromFile(file)) { file =>
        file.getLines().mkString("\n").trim()
      }
    }

  def saveString(file: File, data: String): Either[Throwable, Unit] =
    nonFatalCatch.either {
      Using.resource(new BufferedWriter(new FileWriter(file))) { writer =>
        writer.write(data)
      }
    }

  def changeExtension(file: File, extension: String): File = {
    val filename = {
      val originalFileName = file.getName
      if (originalFileName.contains("."))
        originalFileName.substring(0, originalFileName.lastIndexOf('.'))
      else
        originalFileName
    } + "." + extension

    new File(file.getParentFile, filename)
  }
}
