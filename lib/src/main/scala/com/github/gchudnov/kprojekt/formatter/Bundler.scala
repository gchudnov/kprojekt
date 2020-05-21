package com.github.gchudnov.kprojekt.formatter

import java.io.File

import scala.sys.process.ProcessLogger
import scala.util.Either

/**
 * Bundle encoded data to the output directory derived from path-file.
 */
trait Bundler[T <: Tag] {
  def bundle(isVerbose: Boolean, logger: ProcessLogger, topologyPath: File, data: String): Either[Throwable, File]
}
