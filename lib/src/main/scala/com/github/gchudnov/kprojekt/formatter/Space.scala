package com.github.gchudnov.kprojekt.formatter

sealed trait Space

case object SmallSpace extends Space
case object MediumSpace extends Space
case object LargeSpace extends Space
