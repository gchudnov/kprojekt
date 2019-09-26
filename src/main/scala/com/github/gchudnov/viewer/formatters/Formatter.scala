package gc.learnkafka.trakt

import org.apache.kafka.streams.TopologyDescription.GlobalStore
import org.apache.kafka.streams.TopologyDescription.Node
import org.apache.kafka.streams.TopologyDescription.Processor
import org.apache.kafka.streams.TopologyDescription.Sink
import org.apache.kafka.streams.TopologyDescription.Source
import org.apache.kafka.streams.TopologyDescription.Subtopology

trait Formatter {

  def onSubtopologyStart(st: Subtopology): Formatter
  def onSubtopologyEnd(): Formatter

  def onSuccessor(self: Node, succ: Node): Formatter

  def onSource(s: Source): Formatter
  def onProcessor(p: Processor): Formatter
  def onSink(k: Sink): Formatter

  def onGlobalStore(gs: GlobalStore): Formatter

  def build(): String
}
