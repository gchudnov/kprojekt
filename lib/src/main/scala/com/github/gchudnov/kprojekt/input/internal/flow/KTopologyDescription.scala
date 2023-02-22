package com.github.gchudnov.kprojekt.input.internal.flow

import java.util.{ HashSet => JHashSet, Set => JSet }
import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.TopologyDescription.{ GlobalStore, Subtopology }
import scala.jdk.CollectionConverters._

final class KTopologyDescription() extends TopologyDescription {

  private val subtopologySet: JSet[Subtopology] = new JHashSet[Subtopology]
  private val globalStoreSet: JSet[GlobalStore] = new JHashSet[GlobalStore]

  def addSubtopologies(ss: Seq[Subtopology]): Unit =
    subtopologySet.addAll(ss.asJava)

  def addGlobalStores(gs: Seq[GlobalStore]): Unit =
    globalStoreSet.addAll(gs.asJava)

  override def subtopologies(): JSet[Subtopology] =
    subtopologySet

  override def globalStores(): JSet[GlobalStore] =
    globalStoreSet
}
