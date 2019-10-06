package com.github.gchudnov.presenter

import com.github.gchudnov.presenter.render.Render
import cats.Show
import cats.syntax.show._
import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.TopologyDescription.Node
import org.apache.kafka.streams.TopologyDescription.Processor
import org.apache.kafka.streams.TopologyDescription.Sink
import org.apache.kafka.streams.TopologyDescription.Source
import scala.jdk.CollectionConverters._
import org.apache.kafka.streams.TopologyDescription.Subtopology

/**
  * Present topology with the given builder
  */
object Presenter {

  private val KeySource = "s"
  private val KeyProcessor = "p"
  private val KeySink = "k" 

  def run[A: Render : Show](name: String, desc: TopologyDescription): String = {
    val subtopologies = desc.subtopologies().asScala.toSet
    val topics = collectTopics(subtopologies)
    val topicEdges = collectTopicEdges(subtopologies)

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
          val nodeEdges = collectNodeEdges(nodes)
          val (sources, processors, sinks) = collectNodeByType(nodes)
          val stores = collectStores(processors)
          val storeEdges = collectStoreEdges(processors)
          
          acc
            .subtopologyStart(st.id().toString())
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
        })
      })
      .topologyEnd()
      .get
      .show
  }

  private def collectTopics(subtopologies: Set[Subtopology]): Set[String] = {
    subtopologies.flatMap(_.nodes().asScala.collect({ 
      case s: Source => s.topicSet().asScala.toSet
      case k: Sink => Set(k.topic())
    })).flatten
  }

  private def collectTopicEdges(subtopologies: Set[Subtopology]): Set[(String, String)] = {
     subtopologies.flatMap(_.nodes().asScala.collect({ 
      case s: Source => s.topicSet().asScala.toSet.map((t: String) => (t, s.name()))
      case k: Sink => Set(k.topic()).map(t => (k.name(), t))
    })).flatten
  }

  private def collectNodes(subtopology: Subtopology): Set[Node] = {
    subtopology.nodes().asScala.toSet
  }

  private def collectNodeEdges(nodes: Set[Node]): Set[(String, String)] = {
    nodes.flatMap(from => from.successors().asScala.map(to => (from.name(), to.name())))
  }

  private def collectNodeByType(nodes: Set[Node]): (Set[Source], Set[Processor], Set[Sink]) = {
    val m = nodes.groupBy({
      case _: Source => KeySource
      case _: Processor => KeyProcessor
      case _: Sink => KeySink
    })

    val sources = m.get(KeySource).getOrElse(Set.empty[Node]).map(_.asInstanceOf[Source])
    val processors = m.get(KeyProcessor).getOrElse(Set.empty[Node]).map(_.asInstanceOf[Processor])
    val sinks = m.get(KeySink).getOrElse(Set.empty[Node]).map(_.asInstanceOf[Sink])

    (sources, processors, sinks)
  }

  private def collectStores(processors: Set[Processor]): Set[String] = {
    processors.foldLeft(Set.empty[String])((acc, p) => {
      acc ++ p.stores().asScala
    })
  }

  private def collectStoreEdges(processors: Set[Processor]): Set[(String, String)] = {
    processors.foldLeft(Set.empty[(String, String)])((acc, p) => {
      acc ++ p.stores().asScala.map(storeName => (p.name(), storeName))
    })
  }
}
