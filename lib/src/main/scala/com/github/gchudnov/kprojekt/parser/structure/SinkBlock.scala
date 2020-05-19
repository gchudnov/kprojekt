package com.github.gchudnov.kprojekt.parser.structure

import org.apache.kafka.streams.TopologyDescription.Sink
import org.apache.kafka.streams.processor.TopicNameExtractor

class SinkBlock(sinkName: String, sinkTopic: String) extends NodeBlock(sinkName) with Sink {
  override def topic(): String                                    = sinkTopic
  override def topicNameExtractor(): TopicNameExtractor[Any, Any] = null
}
