package com.github.gchudnov.kprojekt.util

import java.io.{ BufferedWriter, File, FileOutputStream, FileWriter }
import java.nio.file.{Files => JFiles}

import scala.io.Source
import scala.util.Using
import scala.util.control.Exception._

object Dirs {

  def makeTempDir(prefix: String): Either[Throwable, File] =
    nonFatalCatch.either {
      JFiles.createTempDirectory(prefix).toFile
    }

}