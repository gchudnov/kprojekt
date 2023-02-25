package com.github.gchudnov.kprojekt.input.internal.flow

import java.util.regex.Pattern
import java.util.{ Set => JSet }
import org.apache.kafka.streams.TopologyDescription.Source
import scala.jdk.CollectionConverters._

final class KSource(sourceName: String, sourceTopics: Seq[String]) extends KNode(sourceName) with Source {

  override def topicSet(): JSet[String] =
    sourceTopics.toSet.asJava

  override def topicPattern(): Pattern =
    null
}
