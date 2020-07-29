package com.github.gchudnov.kprojekt.naming
import zio.UIO

final class EmptyNamer() extends Namer.Service {
  private val emptyName = NodeName(None, "", "")

  override def name(input: String): UIO[NodeName] = UIO(get(input))
  override def get(input: String): NodeName       = emptyName
}
