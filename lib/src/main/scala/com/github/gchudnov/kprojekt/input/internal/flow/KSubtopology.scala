package com.github.gchudnov.kprojekt.input.internal.flow

import java.util.{ HashSet => JHashSet, Set => JSet }
import org.apache.kafka.streams.TopologyDescription.{ Node, Subtopology }
import scala.jdk.CollectionConverters._

final class KSubtopology(sid: Int) extends Subtopology {

  private val nodeSet: JSet[Node] = new JHashSet[Node]

  def addNodes(ns: Seq[Node]): Unit =
    nodeSet.addAll(ns.asJava)

  override def id(): Int =
    sid

  override def nodes(): JSet[Node] =
    nodeSet
}
