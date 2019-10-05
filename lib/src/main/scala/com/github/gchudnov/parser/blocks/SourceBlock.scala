package com.github.gchudnov.parser.blocks
import org.apache.kafka.streams.TopologyDescription.Source
import java.util.{Set => JSet}
import scala.jdk.CollectionConverters._
import java.util.regex.Pattern

class SourceBlock(sourceName: String, sourceTopics: Seq[String]) extends NodeBlock(sourceName) with Source {
  override def topics(): String = sourceTopics.mkString(",")
  override def topicSet(): JSet[String] = sourceTopics.toSet.asJava
  override def topicPattern(): Pattern = null
}
