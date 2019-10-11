package com.github.gchudnov.presenter

import cats.Show
import cats.syntax.show._
import com.github.gchudnov.presenter.render.Render
import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.TopologyDescription.Node
import org.apache.kafka.streams.TopologyDescription.Processor
import org.apache.kafka.streams.TopologyDescription.Sink
import org.apache.kafka.streams.TopologyDescription.Source
import org.apache.kafka.streams.TopologyDescription.Subtopology
import scala.jdk.CollectionConverters._

/**
  * Present topology with the given builder
  */
object Presenter {

  private val KeySource = "s"
  private val KeyProcessor = "p"
  private val KeySink = "k" 

  def run[A: Render : Show](name: String, desc: TopologyDescription): String = {
    val subtopologies = desc.subtopologies().asScala.toSeq
    val globalStores = desc.globalStores().asScala.toSeq

    val maybeTopicRelatedNodes = subtopologies.flatMap(_.nodes().asScala) ++ globalStores.map(_.source())
    val topics = collectTopics(maybeTopicRelatedNodes)
    val topicEdges = collectTopicEdges(maybeTopicRelatedNodes)

    implicitly[Render[A]]
      .topologyStart(name)
      .topics(ra => {
        topics.foldLeft(ra)((acc, t) => {
          acc.topic(t)
        })
      })
      .edges(ra => {
        topicEdges.foldLeft(ra)((acc, e) => {
          acc.edge(e._1, e._2)
        })
      })
      .subtopologies(ra => {
        subtopologies.foldLeft(ra)((acc, st) => {
          val nodes = collectNodes(st)
          val (sources, processors, sinks) = collectNodeByType(nodes)
          renderSubtopology(acc)(st.id().toString, sources, processors, sinks)
        })
      })
      .subtopologies(ra => {
        globalStores.foldLeft(ra)((acc, gs) => {
          val sources = Seq(gs.source())
          val processors = Seq(gs.processor())
          val sinks = Seq.empty[Sink]
          renderSubtopology(acc)(gs.id().toString, sources, processors, sinks)
        })
      })
      .topologyEnd()
      .get
      .show
  }

  private def renderSubtopology[A: Render](ra: Render[A])(stName: String, sources: Seq[Source], processors: Seq[Processor], sinks: Seq[Sink]): Render[A] = {
    val nodeEdges = collectNodeEdges(sources ++ processors ++ sinks.asInstanceOf[Seq[Node]])
    val stores = collectStores(processors)
    val storeEdges = collectStoreEdges(processors)

    ra
      .storeEdges(storeEdges)
      .subtopologyStart(stName)
      .edges(ra => {
        nodeEdges.foldLeft(ra)((acc, e) => {
          acc.edge(e._1, e._2)
        })
      })
      .sources(ra => {
        sources.foldLeft(ra)((acc, s) => {
          acc.source(s.name, s.topicSet().asScala.toSeq)
        })
      })
      .processors(ra => {
        processors.foldLeft(ra)((acc, p) => {
          acc.processor(p.name(), p.stores().asScala.toSeq)
        })
      })
      .sinks(ra => {
        sinks.foldLeft(ra)((acc, k) => {
          acc.sink(k.name(), k.topic())
        })
      })
      .stores(ra => {
        stores.foldLeft(ra)((acc, storeName) => {
          acc.store(storeName)
        })
      })
      .edges(ra => {
        storeEdges.foldLeft(ra)((acc, e) => {
          acc
          .edge(e._1, e._2)
          .rank(e._1, e._2)
        })
      })
      .subtopologyEnd()
  }

  private def collectTopics(nodes: Seq[Node]): Seq[String] = {
    nodes.collect({ 
      case s: Source => s.topicSet().asScala.toSet
      case k: Sink => Set(k.topic())
    }).flatten.sorted
  }

  private def collectTopicEdges(nodes: Seq[Node]): Seq[(String, String)] = {
     nodes.collect({ 
      case s: Source => s.topicSet().asScala.toSet.map((t: String) => (t, s.name()))
      case k: Sink => Set(k.topic()).map(t => (k.name(), t))
    }).flatten.sorted
  }

  private def collectNodes(subtopology: Subtopology): Seq[Node] = {
    subtopology.nodes().asScala.toSeq
  }

  private def collectNodeEdges(nodes: Seq[Node]): Seq[(String, String)] = {
    nodes.flatMap(from => from.successors().asScala.map(to => (from.name(), to.name()))).sorted
  }

  private def collectNodeByType(nodes: Seq[Node]): (Seq[Source], Seq[Processor], Seq[Sink]) = {
    val m = nodes.groupBy({
      case _: Source => KeySource
      case _: Processor => KeyProcessor
      case _: Sink => KeySink
    })

    val sources = m.getOrElse(KeySource, Set.empty[Node]).map(_.asInstanceOf[Source])
    val processors = m.getOrElse(KeyProcessor, Set.empty[Node]).map(_.asInstanceOf[Processor])
    val sinks = m.getOrElse(KeySink, Set.empty[Node]).map(_.asInstanceOf[Sink])

    (sources.toSeq.sortBy(_.name()), processors.toSeq.sortBy(_.name()), sinks.toSeq.sortBy(_.name()))
  }

  private def collectStores(processors: Seq[Processor]): Seq[String] = {
    processors.foldLeft(Set.empty[String])((acc, p) => {
      acc ++ p.stores().asScala
    }).toSeq.sorted
  }

  private def collectStoreEdges(processors: Seq[Processor]): Seq[(String, String)] = {
    processors.foldLeft(Set.empty[(String, String)])((acc, p) => {
      acc ++ p.stores().asScala.map(storeName => (p.name(), storeName))
    }).toSeq.sorted
  }
}
