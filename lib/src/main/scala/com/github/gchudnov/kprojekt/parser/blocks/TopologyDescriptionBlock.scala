package com.github.gchudnov.kprojekt.parser.blocks

import java.util.{Set => JSet, HashSet => JHashSet}
import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.TopologyDescription.{Subtopology, GlobalStore}
import scala.jdk.CollectionConverters._

class TopologyDescriptionBlock() extends TopologyDescription {

  private val subtopologySet: JSet[Subtopology] = new JHashSet[Subtopology]
  private val globalStoreSet: JSet[GlobalStore] = new JHashSet[GlobalStore]

  def addSubtopologies(ss: Seq[Subtopology]): Unit = {
    subtopologySet.addAll(ss.asJava)
  }

  def addGlobalStores(gs: Seq[GlobalStore]): Unit = {
    globalStoreSet.addAll(gs.asJava)
  }
  override def subtopologies(): JSet[Subtopology] = subtopologySet
  override def globalStores(): JSet[GlobalStore] = globalStoreSet
}
