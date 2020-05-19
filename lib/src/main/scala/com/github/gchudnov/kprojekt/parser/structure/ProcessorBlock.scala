package com.github.gchudnov.kprojekt.parser.structure

import java.util.{ Set => JSet }

import org.apache.kafka.streams.TopologyDescription.Processor

import scala.jdk.CollectionConverters._

class ProcessorBlock(procName: String, stores: Seq[String]) extends NodeBlock(procName) with Processor {
  override def stores(): JSet[String] = stores.toSet.asJava
}
