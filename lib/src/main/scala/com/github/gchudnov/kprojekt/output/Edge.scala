package com.github.gchudnov.kprojekt.output

import scala.math.Ordering

/**
 * An edge between two identifiers
 */
final case class Edge(
  from: Id,
  to: Id
)

object Edge {
  import Id._

  implicit val edgeOrdering: Ordering[Edge] =
    Ordering.by[Edge, (Id, Id)](e => (e.from, e.to))

}
