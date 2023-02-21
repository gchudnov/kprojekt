package com.github.gchudnov.kprojekt.input.internal.flow

import java.util.{ Set => JSet }
import org.apache.kafka.streams.TopologyDescription.Processor
import scala.jdk.CollectionConverters._

final class KProcessor(procName: String, stores: Seq[String]) extends KNode(procName) with Processor {
  override def stores(): JSet[String] = 
    stores.toSet.asJava
}
