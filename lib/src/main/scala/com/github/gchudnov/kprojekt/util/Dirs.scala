package com.github.gchudnov.kprojekt.util

import java.io.File
import java.nio.file.{ Files => JFiles }

import scala.util.control.Exception._

object Dirs {

  def makeTemp(prefix: String): Either[Throwable, File] =
    nonFatalCatch.either {
      JFiles.createTempDirectory(prefix).toFile
    }

}
