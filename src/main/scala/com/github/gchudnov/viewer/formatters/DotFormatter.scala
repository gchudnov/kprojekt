package gc.learnkafka.trakt

import scala.jdk.CollectionConverters._

import org.apache.kafka.streams.TopologyDescription.GlobalStore
import org.apache.kafka.streams.TopologyDescription.Node
import org.apache.kafka.streams.TopologyDescription.Processor
import org.apache.kafka.streams.TopologyDescription.Sink
import org.apache.kafka.streams.TopologyDescription.Source
import org.apache.kafka.streams.TopologyDescription.Subtopology

/**
  * Format Topology for GraphViz (Dot-Format)
  *
  * http://www.graphviz.org/
  * https://graphviz.gitlab.io/_pages/doc/info/shapes.html
  * https://graphviz.gitlab.io/_pages/pdf/dotguide.pdf
  *
  * cat graph.dot | dot -Tpng > graph.png
  */
class DotFormatter(b: StringBuilder = new StringBuilder) extends Formatter {

  override def onSubtopologyStart(st: Subtopology): Formatter = {
    val topologyId = st.id().toString()
    new DotFormatter(b.append(s"digraph G${topologyId} {\n"))
  }

  override def onSubtopologyEnd(): Formatter = {
    new DotFormatter(b.append(s"fontsize=${DotFormatter.FontSize};\n").append("}\n"))
  }

  override def onSuccessor(self: Node, succ: Node): Formatter = {
    val nodeName = self.name()
    val nodeId = toId(nodeName)

    val succName = succ.name()
    val succId = toId(succName)

    new DotFormatter(b.append(s"${nodeId} -> ${succId};\n"))
  }

  override def onSource(s: Source): Formatter = {
    val nodeName = s.name()
    val nodeId = toId(nodeName)

    val topics = s.topicSet().asScala.toSet
    val topicPattern = Option(s.topicPattern())
    val topicsStr = topics.mkString(",")

    val label = topicPattern
      .foldLeft(List(nodeName, s"topics: ${topicsStr}"))((acc, pattern) => {
        acc :+ s"pattern: ${pattern.toString()}"
      })
      .mkString("\\n")

    new DotFormatter(b.append(s"""${nodeId} [shape=ellipse, label="${label}"];\n"""))
  }

  override def onProcessor(p: Processor): Formatter = {
    val nodeName = p.name()
    val nodeId = toId(nodeName)

    val stores = p.stores().asScala.toSet

    val label = (List(nodeName) ++ {
      if (stores.nonEmpty) {
        List("stores: " + stores.mkString(","))
      } else {
        List.empty[String]
      }
    }).mkString("\\n")

    new DotFormatter(b.append(s"""${nodeId} [shape=box, label="${label}"];\n"""))
  }

  override def onSink(k: Sink): Formatter = {
    val nodeName = k.name()
    val nodeId = toId(nodeName)

    val topic = Option(k.topic())
    val topicNameExtractor = Option(k.topicNameExtractor())

    val topicOrExtractor = (topic, topicNameExtractor) match {
      case (Some(t), _) =>
        s"topic: ${t}"
      case (_, Some(e)) =>
        s"has_extractor"
      case _ =>
        s""
    }

    val label = List(nodeName, topicOrExtractor).mkString("\\n")

    new DotFormatter(b.append(s"""${nodeId} [shape=ellipse, label="${label}"];\n"""))
  }

  override def onGlobalStore(gs: GlobalStore): Formatter = {
    val globalStoreId = gs.id()
    val s = gs.source()
    val p = gs.processor()

    b.append(s"subgraph globalStore${globalStoreId} {\n")
    onSource(s)
    onProcessor(p)
    b.append("}\n")

    new DotFormatter(b)
  }

  override def build(): String = {
    b.toString()
  }

  private def toId(name: String): String = {
    name.replace("-", "_")
  }
}

object DotFormatter {
  val FontSize = 10

  def apply(): DotFormatter = new DotFormatter()
}
