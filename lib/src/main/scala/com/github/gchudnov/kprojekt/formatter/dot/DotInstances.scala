package com.github.gchudnov.kprojekt.formatter.dot

import com.github.gchudnov.kprojekt.formatter.{ Bundler, Encoder }

sealed trait DotInstances {
  implicit val dotEncoder: Encoder[Dot] = DotFormat()
  implicit val dotBundler: Bundler[Dot] = DotBundler()
}

object DotInstances extends DotInstances
