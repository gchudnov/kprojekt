package com.github.gchudnov.kprojekt.util

import java.io.{ File, FileOutputStream }

import scala.io.Source
import scala.util.Using
import scala.util.control.Exception._

object Resources {

  def save(file: File, resourcePath: String): Either[Throwable, Long] =
    nonFatalCatch.either {
      val classLoader: ClassLoader = getClass.getClassLoader
      Using.resources(classLoader.getResourceAsStream(resourcePath), new FileOutputStream(file)) { (inStream, outStream) =>
        inStream.transferTo(outStream)
      }
    }.left.map(t => new RuntimeException(s"Cannot save the resource '$resourcePath' to '${file.getAbsolutePath}'", t))

  def lines(resourcePath: String): Either[Throwable, String] =
    nonFatalCatch.either {
      Using.resource(Source.fromResource(resourcePath)) { source =>
        source.getLines().mkString("\n").trim()
      }
    }

}
