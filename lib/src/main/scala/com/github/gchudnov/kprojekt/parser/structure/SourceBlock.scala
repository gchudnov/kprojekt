package com.github.gchudnov.kprojekt.parser.structure

import java.util.regex.Pattern
import java.util.{ Set => JSet }

import org.apache.kafka.streams.TopologyDescription.Source

import scala.jdk.CollectionConverters._

class SourceBlock(sourceName: String, sourceTopics: Seq[String]) extends NodeBlock(sourceName) with Source {
  override def topics(): String         = sourceTopics.mkString(",")
  override def topicSet(): JSet[String] = sourceTopics.toSet.asJava
  override def topicPattern(): Pattern  = null
}
