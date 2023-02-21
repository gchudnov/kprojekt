package com.github.gchudnov.kprojekt.util

import java.io.{ BufferedWriter, File, FileOutputStream, FileWriter }
import java.nio.file.{Files => JFiles}

import scala.io.Source
import scala.util.Using
import scala.util.control.Exception._

object Resources {

  def saveResource(file: File, resourcePath: String): Either[Throwable, Long] =
    nonFatalCatch.either {
      val classLoader: ClassLoader = getClass.getClassLoader
      Using.resources(classLoader.getResourceAsStream(resourcePath), new FileOutputStream(file)) { (inStream, outStream) =>
        inStream.transferTo(outStream)
      }
    }.left.map(t => new RuntimeException(s"Cannot save the resource '$resourcePath' to '${file.getAbsolutePath}'", t))

  def linesFromResource(resourcePath: String): Either[Throwable, String] =
    nonFatalCatch.either {
      Using.resource(Source.fromResource(resourcePath)) { source =>
        source.getLines().mkString("\n").trim()
      }
    }

}
