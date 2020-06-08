package com.github.gchudnov.kprojekt.encoder

import com.github.gchudnov.kprojekt.formatter.Folder
import com.github.gchudnov.kprojekt.naming.{ Legend, Namer }
import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.TopologyDescription._
import zio.{ UIO, ZIO }

import scala.jdk.CollectionConverters._

/**
 * Encodes TopologyDescription to a string.
 */
final class LiveEncoder(folder: Folder.Service, namer: Namer.Service) extends Encoder.Service {
  import LiveEncoder._

  override def encode(name: String, desc: TopologyDescription): UIO[String] = {
    val subtopologies = desc.subtopologies().asScala.toSeq.sortBy(_.id())
    val globalStores  = desc.globalStores().asScala.toSeq.sortBy(_.id())

    val maybeTopicRelatedNodes = subtopologies.flatMap(_.nodes().asScala) ++ globalStores.map(_.source())
    val topics                 = collectTopics(maybeTopicRelatedNodes)
    val topicEdges             = collectTopicEdges(maybeTopicRelatedNodes)

    for {
      nodeNames <- ZIO.foreach(collectNames(desc))(namer.name)
    } yield folder
      .topologyStart(name)
      .legend(Legend(nodeNames))
      .topics { ra =>
        topics.foldLeft(ra) { (acc, t) =>
          acc.topic(t)
        }
      }
      .edges { ra =>
        topicEdges.foldLeft(ra) { (acc, e) =>
          acc.edge(e._1, e._2)
        }
      }
      .subtopologies { ra =>
        subtopologies.foldLeft(ra) { (acc, st) =>
          val nodes                        = collectNodes(st)
          val (sources, processors, sinks) = collectNodeByType(nodes)
          collectSubtopology(acc)(st.id().toString, sources, processors, sinks)
        }
      }
      .subtopologies { ra =>
        globalStores.foldLeft(ra) { (acc, gs) =>
          val sources    = Seq(gs.source())
          val processors = Seq(gs.processor())
          val sinks      = Seq.empty[Sink]
          collectSubtopology(acc)(gs.id().toString, sources, processors, sinks)
        }
      }
      .topologyEnd()
      .toString
  }

}

object LiveEncoder {
  private val KeySource    = "s"
  private val KeyProcessor = "p"
  private val KeySink      = "k"

  private def collectSubtopology(ra: Folder.Service)(stName: String, sources: Seq[Source], processors: Seq[Processor], sinks: Seq[Sink]): Folder.Service = {
    val nodeEdges  = collectNodeEdges(sources ++ processors ++ sinks.asInstanceOf[Seq[Node]])
    val stores     = collectStores(processors)
    val storeEdges = collectStoreEdges(processors)

    ra.storeEdges(storeEdges)
      .subtopologyStart(stName)
      .edges { ra =>
        nodeEdges.foldLeft(ra) { (acc, e) =>
          acc.edge(e._1, e._2)
        }
      }
      .sources { ra =>
        sources.foldLeft(ra) { (acc, s) =>
          acc.source(s.name, s.topicSet().asScala.toSeq.sorted)
        }
      }
      .processors { ra =>
        processors.foldLeft(ra) { (acc, p) =>
          acc.processor(p.name(), p.stores().asScala.toSeq.sorted)
        }
      }
      .sinks { ra =>
        sinks.foldLeft(ra) { (acc, k) =>
          acc.sink(k.name(), k.topic())
        }
      }
      .stores { ra =>
        stores.foldLeft(ra) { (acc, storeName) =>
          acc.store(storeName)
        }
      }
      .edges { ra =>
        storeEdges.foldLeft(ra) { (acc, e) =>
          acc
            .edge(e._1, e._2)
        }
      }
      .subtopologyEnd()
  }

  private def collectTopics(nodes: Seq[Node]): Seq[String] =
    nodes
      .collect({
        case s: Source => s.topicSet().asScala.toSet
        case k: Sink   => Set(k.topic())
      })
      .flatten
      .distinct
      .sorted

  private def collectTopicEdges(nodes: Seq[Node]): Seq[(String, String)] =
    nodes
      .collect({
        case s: Source => s.topicSet().asScala.toSet.map((t: String) => (t, s.name()))
        case k: Sink   => Set(k.topic()).map(t => (k.name(), t))
      })
      .flatten
      .distinct
      .sorted

  private def collectNodes(subtopology: Subtopology): Seq[Node] =
    subtopology.nodes().asScala.toSeq.distinctBy(_.name()).sortBy(_.name())

  private def collectNodeEdges(nodes: Seq[Node]): Seq[(String, String)] =
    nodes.flatMap(from => from.successors().asScala.map(to => (from.name(), to.name()))).distinct.sorted

  private def collectNodeByType(nodes: Seq[Node]): (Seq[Source], Seq[Processor], Seq[Sink]) = {
    val m = nodes.groupBy({
      case _: Source    => KeySource
      case _: Processor => KeyProcessor
      case _: Sink      => KeySink
    })

    val sources    = m.getOrElse(KeySource, Set.empty[Node]).map(_.asInstanceOf[Source])
    val processors = m.getOrElse(KeyProcessor, Set.empty[Node]).map(_.asInstanceOf[Processor])
    val sinks      = m.getOrElse(KeySink, Set.empty[Node]).map(_.asInstanceOf[Sink])

    (sources.toSeq.distinctBy(_.name()).sortBy(_.name()), processors.toSeq.distinctBy(_.name()).sortBy(_.name()), sinks.toSeq.distinctBy(_.name()).sortBy(_.name()))
  }

  private def collectStores(processors: Seq[Processor]): Seq[String] =
    processors
      .foldLeft(Set.empty[String]) { (acc, p) =>
        acc ++ p.stores().asScala
      }
      .toSeq
      .distinct
      .sorted

  private def collectStoreEdges(processors: Seq[Processor]): Seq[(String, String)] =
    processors
      .foldLeft(Set.empty[(String, String)]) { (acc, p) =>
        acc ++ p.stores().asScala.map(storeName => (p.name(), storeName))
      }
      .toSeq
      .distinct
      .sorted

  private[encoder] def collectNames(desc: TopologyDescription): Seq[String] = {
    val subtopologies = desc.subtopologies().asScala.toSeq
    val globalStores  = desc.globalStores().asScala.toSeq

    val globalStoreNames = globalStores.foldLeft(Seq.empty[String]) { (acc, gs) =>
      acc ++ collectNames(Seq(gs.source()), Seq(gs.processor()), Seq.empty[Sink])
    }

    subtopologies
      .foldLeft(globalStoreNames) { (acc, st) =>
        val ns           = st.nodes().asScala.toSeq
        val (ss, ps, ks) = collectNodeByType(ns)
        acc ++ collectNames(ss, ps, ks)
      }
      .distinct
      .sorted
  }

  private def collectNames(sources: Seq[Source], processors: Seq[Processor], sinks: Seq[Sink]): Seq[String] = {
    val sourceNames = sources.flatMap(s => s.name() +: s.topicSet().asScala.toSeq)
    val procNames   = processors.flatMap(p => p.name() +: p.stores().asScala.toSeq)
    val sinkNames   = sinks.flatMap(k => Seq(k.name(), k.topic()))
    (sourceNames ++ procNames ++ sinkNames)
  }
}
