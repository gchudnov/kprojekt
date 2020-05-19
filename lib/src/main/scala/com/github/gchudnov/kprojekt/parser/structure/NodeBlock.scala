package com.github.gchudnov.kprojekt.parser.structure

import java.util.{ HashSet => JHashSet, Set => JSet }

import org.apache.kafka.streams.TopologyDescription.Node

import scala.jdk.CollectionConverters._

class NodeBlock(blockName: String) extends Node {

  private val succ: JSet[Node] = new JHashSet()
  private val pred: JSet[Node] = new JHashSet()

  def addSuccessors(ss: Seq[Node]): Unit =
    succ.addAll(ss.asJava)

  def addPredecessors(ps: Seq[Node]): Unit =
    pred.addAll(ps.asJava)

  override def name(): String             = blockName
  override def predecessors(): JSet[Node] = pred
  override def successors(): JSet[Node]   = succ
}
