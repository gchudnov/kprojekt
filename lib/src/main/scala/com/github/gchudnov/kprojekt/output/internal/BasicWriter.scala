package com.github.gchudnov.kprojekt.output.internal

import com.github.gchudnov.kprojekt.output.Id
import com.github.gchudnov.kprojekt.output.Edge
import com.github.gchudnov.kprojekt.output.Builder
import com.github.gchudnov.kprojekt.output.Writer
import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.TopologyDescription._
import zio._

import scala.jdk.CollectionConverters._


/**
  * Write Topology using the provided Builder
  *
  * @param builder Builder to use (e.g. DotBuilder)
  */
final class BasicWriter(builder: Builder) extends Writer {
  import BasicWriter._

  override def write(name: String, desc: TopologyDescription): Task[String] =
    ZIO.attempt {
      val subtopologies: List[Subtopology] = desc.subtopologies().asScala.toList.sortBy(_.id())
      val globalStores: List[GlobalStore]  = desc.globalStores().asScala.toList.sortBy(_.id())

      val maybeTopicRelatedNodes: List[Node] = subtopologies.flatMap(_.nodes().asScala) ++ globalStores.map(_.source())
      val topics                 = findTopicIds(maybeTopicRelatedNodes)
      val topicEdges             = findTopicEdges(maybeTopicRelatedNodes)

      val allIds = findTopologyIds(desc)

      builder
        .topologyStart(name)
        .repository(allIds)
        .topics { b =>
          topics.foldLeft(b) { (acc, t) =>
            acc.topic(t)
          }
        }
        .edges { b =>
          topicEdges.foldLeft(b) { (acc, e) =>
            acc.edge(e)
          }
        }
        .subtopologies { b =>
          subtopologies.foldLeft(b) { (acc, st) =>
            val nodes                        = findNodes(st)
            val (sources, processors, sinks) = findDistinctNodesByType(nodes)
            buildSubtopology(acc, st.id().toString, sources, processors, sinks)
          }
        }
        .subtopologies { b =>
          globalStores.foldLeft(b) { (acc, gs) =>
            val sources    = List(gs.source())
            val processors = List(gs.processor())
            val sinks      = List.empty[Sink]
            buildSubtopology(acc, gs.id().toString, sources, processors, sinks)
          }
        }
        .topologyEnd()
        .build
    }

}

object BasicWriter {

  private def buildSubtopology(b: Builder, stName: String, sources: List[Source], processors: List[Processor], sinks: List[Sink]): Builder = {
    val nodeEdges: List[Edge]  = findNodeEdges(sources ++ processors ++ sinks.asInstanceOf[List[Node]])
    val stores: List[Id]       = findStoreIds(processors)
    val storeEdges: List[Edge] = findStoreEdges(processors)

    b.storeEdges(storeEdges)
      .subtopologyStart(stName)
      .edges { b =>
        nodeEdges.foldLeft(b) { (acc, e) =>
          acc.edge(e)
        }
      }
      .sources { b =>
        sources.foldLeft(b) { (acc, s) =>
          val sid = toId(s)
          val ts  = s.topicSet().asScala.toList.map(it => Id.topic(it)).sorted
          acc.source(sid, ts)
        }
      }
      .processors { b =>
        processors.foldLeft(b) { (acc, p) =>
          val pid = toId(p)
          val ss = p.stores().asScala.toList.map(it => Id.store(it)).sorted
          acc.processor(pid, ss)
        }
      }
      .sinks { b =>
        sinks.foldLeft(b) { (acc, k) =>
          val kid = toId(k)
          acc.sink(kid, Id.topic(k.topic()))
        }
      }
      .stores { b =>
        stores.foldLeft(b) { (acc, r) =>
          acc.store(r)
        }
      }
      .edges { b =>
        storeEdges.foldLeft(b) { (acc, e) =>
          acc.edge(e)
        }
      }
      .subtopologyEnd()
  }

  /**
    * Find all topic identifiers
    */
  private def findTopicIds(nodes: List[Node]): List[Id] =
    nodes.collect {
      case s: Source => s.topicSet().asScala.toSet
      case k: Sink   => Set(k.topic())
    }.flatten
      .map(Id.topic)
      .distinct
      .sorted

  /**
    * Find all topic edges: (X -> Topic)
    */
  private def findTopicEdges(nodes: List[Node]): List[Edge] =
    nodes.collect {
      case s: Source => s.topicSet().asScala.toSet.map((t: String) => Edge(Id.topic(t), toId(s)))
      case k: Sink   => Set(k.topic()).map(t => Edge(toId(k), Id.topic(t)))
    }.flatten.distinct.sorted

  /**
    * Find Nodes for subtopology
    */
  private def findNodes(subtopology: Subtopology): List[Node] =
    uniqSort(subtopology.nodes().asScala.toList)

  /**
    * Collects all edges
    */
  private def findNodeEdges(nodes: List[Node]): List[Edge] =
    nodes
      .flatMap(from => from.successors().asScala
      .map(to => Edge(toId(from), toId(to))))
      .distinct
      .sorted

  /**
    * Finds all distinct nodes by type
    */
  private def findDistinctNodesByType(nodes: List[Node]): (List[Source], List[Processor], List[Sink]) = {
    val (sources, processors, sinks) = nodes.foldLeft((List.empty[Source], List.empty[Processor], List.empty[Sink])) {
      case (acc, n) =>
        n match {
          case s: Source    => acc.copy(_1 = acc._1 :+ s)
          case p: Processor => acc.copy(_2 = acc._2 :+ p)
          case k: Sink      => acc.copy(_3 = acc._3 :+ k)
          case _            => sys.error(s"Invalid node type: $n; This is a bug in the library.")
        }
    }

    (uniqSort(sources), uniqSort(processors), uniqSort(sinks))
  }

  /**
    * Find all store identifiers, linked to processors
    */
  private def findStoreIds(processors: List[Processor]): List[Id] =
    processors
      .foldLeft(Set.empty[Id]) { (acc, p) =>
        acc ++ p.stores().asScala.map(it => Id.store(it))
      }
      .toList
      .distinct
      .sorted

  /**
    * Collect all store edges
    */
  private def findStoreEdges(processors: List[Processor]): List[Edge] =
    processors
      .foldLeft(Set.empty[Edge]) { (acc, p) =>
        acc ++ p.stores().asScala.map(storeName => Edge(toId(p), Id.store(storeName)))
      }
      .toList
      .distinct
      .sorted

  /**
    * Extract all Ids from a topology
    */
  private def findTopologyIds(desc: TopologyDescription): List[Id] = {
    val subtopologies: List[Subtopology] = desc.subtopologies().asScala.toList
    val globalStores: List[GlobalStore]  = desc.globalStores().asScala.toList

    val globalStoreIds: List[Id] = globalStores.foldLeft(List.empty[Id]) { (acc, gs) =>
      acc ++ findIds(List(gs.source()), List(gs.processor()), List.empty[Sink])
    }

    subtopologies
      .foldLeft(globalStoreIds) { case (acc, st) =>
        val ns           = st.nodes().asScala.toList
        val (ss, ps, ks) = findDistinctNodesByType(ns)
        acc ++ findIds(ss, ps, ks)
      }
      .distinct
      .sorted
  }

  /**
    * Given a collection of sources, processors and sinks, extract Ids
    */
  private def findIds(sources: List[Source], processors: List[Processor], sinks: List[Sink]): List[Id] = {
    val sourceIds = sources.flatMap(s => toId(s) +: s.topicSet().asScala.toList.map(it => Id.topic(it)))
    val procIds   = processors.flatMap(p => toId(p) +: p.stores().asScala.toList.map(it => Id.store(it)))
    val sinkIds   = sinks.flatMap(k => List(toId(k), Id.topic(k.topic())))
    
    sourceIds ++ procIds ++ sinkIds
  }

  /**
    * Extract Id from a Node
    */
  private def toId(node: Node): Id =
    node match {
      case s: Source =>
        Id.source(s.name())
      case p: Processor =>
        Id.processor(p.name())
      case k: Sink =>
        Id.sink(k.name())
      case _ => sys.error(s"Invalid node type: $node; This is a bug in the library.")
    }

  private def uniqSort[N <: Node](ns: List[N]): List[N] =
    ns.distinctBy(_.name()).sortBy(_.name())
}
