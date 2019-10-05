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

/**
  * Present topology with the given builder
  */
object Presenter {

  private val KeySource = "s"
  private val KeyProcessor = "p"
  private val KeySink = "k" 

  def run[A: Render : Show](name: String, desc: TopologyDescription): String = {
    val subtopologies = desc.subtopologies().asScala.toSet

    val topics = subtopologies.flatMap(_.nodes().asScala.collect({ 
      case s: Source => s.topicSet().asScala.toSet
      case k: Sink => Set(k.topic())
    })).flatten

    val topicEdges = subtopologies.flatMap(_.nodes().asScala.collect({ 
      case s: Source => s.topicSet().asScala.toSet.map((t: String) => (t, s.name()))
      case k: Sink => Set(k.topic()).map(t => (k.name(), t))
    })).flatten

    println(topicEdges)

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
          val nodes = st.nodes().asScala.toSet
          val edges = nodes.flatMap(from => from.successors().asScala.map(to => (from, to)))
          val m = nodes.groupBy({
            case _: Source => KeySource
            case _: Processor => KeyProcessor
            case _: Sink => KeySink
          })
          val sources = m.get(KeySource).getOrElse(Set.empty[Node]).map(_.asInstanceOf[Source])
          val processors = m.get(KeyProcessor).getOrElse(Set.empty[Node]).map(_.asInstanceOf[Processor])
          val sinks = m.get(KeySink).getOrElse(Set.empty[Node]).map(_.asInstanceOf[Sink])
          
          acc
            .subtopologyStart(st.id().toString())
            .edges(ra => {
              edges.foldLeft(ra)((acc, e) => {
                acc.edge(e._1.name(), e._2.name())
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
            .subtopologyEnd()
        })
      })
      .topologyEnd()
      .get
      .show
  }
}
