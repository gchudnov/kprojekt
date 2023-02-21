package com.github.gchudnov.kprojekt.input.internal.flow

import org.apache.kafka.streams.TopologyDescription.Sink
import org.apache.kafka.streams.processor.TopicNameExtractor

final class KSink(sinkName: String, sinkTopic: String) extends KNode(sinkName) with Sink {
  override def topic(): String                                    = 
    sinkTopic

  override def topicNameExtractor(): TopicNameExtractor[Any, Any] = 
    null
}
