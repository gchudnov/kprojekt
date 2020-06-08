package com.github.gchudnov.kprojekt.naming

final case class LegendEntry(id: Option[Int], alias: String, name: String)

trait Legend {
  def nodeName(name: String): Option[NodeName]
  def table: Seq[LegendEntry]
}

final class BasicLegend(nodeNames: Seq[NodeName]) extends Legend {
  private val m: Map[String, NodeName] = nodeNames.map(it => (it.original -> it)).toMap

  override def nodeName(name: String): Option[NodeName] =
    m.get(name)

  override def table: Seq[LegendEntry] =
    nodeNames.map(it => LegendEntry(id = it.id, alias = it.alias, name = it.original)).sortBy(it => (it.id, it.alias))
}

object Legend {
  def apply(nodeNames: Seq[NodeName]): Legend =
    new BasicLegend(nodeNames)

  val empty: Legend = new BasicLegend(Seq.empty[NodeName])
}
