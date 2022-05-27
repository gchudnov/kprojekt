package com.github.gchudnov.kprojekt.naming
import zio._

final class EmptyNamer() extends Namer {
  private val emptyName = NodeName(None, "", "")

  override def name(input: String): UIO[NodeName] = ZIO.succeed(get(input))
  override def get(input: String): NodeName       = emptyName
}
