package com.github.gchudnov.kprojekt.util

import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.nio.file.Files
import scala.io.Source
import scala.util.Try
import scala.util.Using

object FileOps {

  def createTempDir(prefix: String): File = {
    Files.createTempDirectory(prefix).toFile
  }

  def stringFromFile(file: File): String = {
    Using.resource(Source.fromFile(file)) { file =>
      file.getLines.mkString("\n").trim()
    }
  }

  def stringFromResource(resourcePath: String): String = {
    Using.resource(Source.fromResource(resourcePath)) { source =>
      source.getLines.mkString("\n").trim()
    }
  }

  def saveString(file: File)(data: String): Try[Unit] = {
    Using(new BufferedWriter(new FileWriter(file))) { writer =>
      writer.write(data)
    }
  }

  def saveResource(file: File)(resourcePath: String): Unit = {
    val classLoader: ClassLoader = Thread.currentThread().getContextClassLoader()
    Using.resources(classLoader.getResourceAsStream(resourcePath), new FileOutputStream(file)) { (inStream, outStream) =>
      inStream.transferTo(outStream)
    }
  }

}
