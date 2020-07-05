package com.github.gchudnov.kprojekt.util

import java.io.{ BufferedWriter, File, FileOutputStream, FileWriter }
import java.nio.file.Files

import cats.syntax.either._

import scala.io.Source
import scala.util.Using

object FileOps {

  def createTempDir(prefix: String): Either[Throwable, File] =
    Either.catchNonFatal {
      Files.createTempDirectory(prefix).toFile
    }

  def stringFromFile(file: File): Either[Throwable, String] =
    Either.catchNonFatal {
      Using.resource(Source.fromFile(file)) { file =>
        file.getLines().mkString("\n").trim()
      }
    }

  def stringFromResource(resourcePath: String): Either[Throwable, String] =
    Either.catchNonFatal {
      Using.resource(Source.fromResource(resourcePath)) { source =>
        source.getLines().mkString("\n").trim()
      }
    }

  def saveString(file: File)(data: String): Either[Throwable, Unit] =
    Either.catchNonFatal {
      Using.resource(new BufferedWriter(new FileWriter(file))) { writer =>
        writer.write(data)
      }
    }

  def saveResource(file: File)(resourcePath: String): Either[Throwable, Long] =
    Either.catchNonFatal {
      val classLoader: ClassLoader = getClass.getClassLoader
      Using.resources(classLoader.getResourceAsStream(resourcePath), new FileOutputStream(file)) { (inStream, outStream) =>
        inStream.transferTo(outStream)
      }
    }.leftMap(t => new RuntimeException(s"Cannot save the resource '${resourcePath}' to '${file.getAbsolutePath}'", t))

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
