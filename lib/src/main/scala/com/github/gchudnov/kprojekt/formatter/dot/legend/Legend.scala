package com.github.gchudnov.kprojekt.formatter.dot.legend

import com.github.gchudnov.kprojekt.ids.NodeId
import com.github.gchudnov.kprojekt.naming.{ EmptyNamer, Namer, NodeName }

trait Legend {
  def entry(tId: String): Option[NodeName]
  def table: Seq[NodeName]
}

final class BasicLegend(namer: Namer, nodeIds: Seq[NodeId]) extends Legend {
  private val nodeMap: Map[String, NodeName] = nodeIds.map(nodeId => (nodeId.tId -> namer.get(nodeId.name))).toMap

  override def entry(tId: String): Option[NodeName] =
    nodeMap.get(tId)

  override def table: Seq[NodeName] =
    nodeMap.values.toSeq.sortBy(it => (it.id, it.alias))
}

object Legend {
  def apply(namer: Namer, nodeIds: Seq[NodeId]): Legend =
    new BasicLegend(namer, nodeIds)

  val empty: Legend = new BasicLegend(namer = new EmptyNamer(), nodeIds = Seq.empty[NodeId])
}
