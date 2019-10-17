package com.github.gchudnov.kprojekt.parser.blocks

import org.apache.kafka.streams.processor.TopicNameExtractor
import org.apache.kafka.streams.TopologyDescription.Sink

class SinkBlock(sinkName: String, sinkTopic: String) extends NodeBlock(sinkName) with Sink {
  override def topic(): String = sinkTopic
  override def topicNameExtractor(): TopicNameExtractor[Any, Any] = null
}
