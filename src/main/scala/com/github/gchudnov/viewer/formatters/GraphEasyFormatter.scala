package gc.learnkafka.trakt

import org.apache.kafka.streams.TopologyDescription

/**
  * Formats the Topology to process with GraphEasy
  *
  *
  *
  */
class EasyGraphFormatter() extends Formatter {

  override def onSubtopologyStart(st: TopologyDescription.Subtopology): Formatter = ???

  override def onSubtopologyEnd(): Formatter = ???

  override def onSuccessor(self: TopologyDescription.Node, succ: TopologyDescription.Node): Formatter = ???

  override def onSource(s: TopologyDescription.Source): Formatter = ???

  override def onProcessor(p: TopologyDescription.Processor): Formatter = ???

  override def onSink(k: TopologyDescription.Sink): Formatter = ???

  override def onGlobalStore(gs: TopologyDescription.GlobalStore): Formatter = ???

  override def build(): String = ???

}

object EasyGraphFormatter {}
