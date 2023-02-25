package com.github.gchudnov.kprojekt.input.internal.flow

import java.util.{ HashSet => JHashSet, Set => JSet }
import org.apache.kafka.streams.TopologyDescription.Node
import scala.jdk.CollectionConverters._

class KNode(blockName: String) extends Node {

  private val succ: JSet[Node] = new JHashSet()
  private val pred: JSet[Node] = new JHashSet()

  def addSuccessors(ss: Seq[Node]): Unit =
    succ.addAll(ss.asJava)

  def addPredecessors(ps: Seq[Node]): Unit =
    pred.addAll(ps.asJava)

  override def name(): String =
    blockName

  override def predecessors(): JSet[Node] =
    pred

  override def successors(): JSet[Node] =
    succ
}
