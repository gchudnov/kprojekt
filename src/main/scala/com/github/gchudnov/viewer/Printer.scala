package gc.learnkafka.trakt

import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.TopologyDescription.Processor
import org.apache.kafka.streams.TopologyDescription.Sink
import org.apache.kafka.streams.TopologyDescription.Source
import scala.jdk.CollectionConverters._

/**
  * Topology Printer with the given Formatter
  *
  */
object Printer {

  def print(desc: TopologyDescription, formatter: Formatter): String = {
    val subtopologies = desc.subtopologies().asScala.toSet

    val withSubtopologies = subtopologies.foldLeft(formatter)((acc, subtopology) => {
      val nodes = subtopology.nodes().asScala.toSet

      val res = nodes.foldLeft(acc.onSubtopologyStart(subtopology))((acc, node) => {
        val successors = node.successors().asScala.toSet

        val withSuccessors = successors.foldLeft(acc)((acc, succ) => {
          acc.onSuccessor(node, succ)
        })

        node match {
          case s: Source =>
            withSuccessors.onSource(s)

          case p: Processor =>
            withSuccessors.onProcessor(p)

          case k: Sink =>
            withSuccessors.onSink(k)

          case _ =>
            withSuccessors
        }
      })

      res.onSubtopologyEnd()
    })

    val globalStores = desc.globalStores().asScala.toSet

    val withGlobalStores = globalStores.foldLeft(withSubtopologies)((acc, globalStore) => {
      acc.onGlobalStore(globalStore)
    })

    withGlobalStores.build()
  }
}
