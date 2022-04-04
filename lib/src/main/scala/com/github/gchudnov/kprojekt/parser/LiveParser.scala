package com.github.gchudnov.kprojekt.parser

import com.github.gchudnov.kprojekt.parser.structure._
import fastparse.MultiLineWhitespace._
import fastparse._
import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.TopologyDescription.{ Node, Subtopology }
import zio.{ Has, Task, ZIO, ZLayer }

/**
 * Parses the text description of the Topology
 */
final class LiveParser() extends Parser {
  import LiveParser._

  override def run(input: String): Task[TopologyDescription] =
    ZIO.fromEither {
      parse(input, topology(_)).fold(
        (msg, pos, _) => Left[ParseException, TopologyDescription](new ParseException(s"Cannot parse input: $msg at $pos")),
        (t, _) => Right[ParseException, TopologyDescription](toTopologyDescription(t))
      )
    }
}

object LiveParser {
  sealed trait NodeRef

  final case class TopologyRef(subtopologies: Seq[SubtopologyRef])
  final case class SubtopologyRef(name: String, nodes: Seq[NodeRef])
  final case class SourceRef(name: String, topics: Seq[String], next: Seq[String])                       extends NodeRef
  final case class ProcessorRef(name: String, stores: Seq[String], next: Seq[String], prev: Seq[String]) extends NodeRef
  final case class SinkRef(name: String, topic: String, prev: Seq[String])                               extends NodeRef

  private def untilEol[_: P] = P(CharsWhile(_ != '\n', 0))

  private def space[_: P] = P(CharsWhileIn(" \r\n", 0))

  private def next[_: P] = P("-->")
  private def prev[_: P] = P("<--")

  private def identifier[_: P]    = P(CharsWhileIn("0-9a-zA-Z\\-_.").!)
  private def identifierSeq[_: P] = P(identifier.rep(sep = ("," ~ space)./))

  private def storeSeq[_: P] = P("stores:" ~/ "[" ~/ identifierSeq ~ "]")
  private def topicSeq[_: P] = P("topics:" ~/ "[" ~/ identifierSeq ~ "]")
  private def topic[_: P]    = P("topic:" ~/ identifier)

  private def source[_: P]    = P("Source:" ~/ identifier ~ "(" ~/ topicSeq ~ ")" ~/ next ~/ identifierSeq).map(SourceRef.tupled)
  private def processor[_: P] = P("Processor:" ~/ identifier ~ "(" ~/ storeSeq ~ ")" ~/ next ~/ identifierSeq ~ prev ~/ identifierSeq).map(ProcessorRef.tupled)
  private def sink[_: P]      = P("Sink:" ~/ identifier ~ "(" ~/ topic ~ ")" ~/ prev ~/ identifierSeq).map(SinkRef.tupled)

  private def subtopologyId[_: P] = P("Sub-topology:" ~/ identifier ~~ untilEol)
  private def subtopology[_: P]   = P(subtopologyId ~ (source | processor | sink).rep).map(SubtopologyRef.tupled)

  private def topology[_: P] = P("Topologies:" ~/ subtopology.rep).map(TopologyRef)

  def layer: ZLayer[Any, Nothing, Has[Parser]] = ZLayer.succeed(new LiveParser())

  private def toTopologyDescription(topoRef: TopologyRef): TopologyDescription = {
    val ss   = topoRef.subtopologies.map(buildSubtopology)
    val desc = new TopologyDescriptionBlock()
    desc.addSubtopologies(ss)
    desc
  }

  private def buildSubtopology(st: SubtopologyRef): Subtopology = {
    import com.github.gchudnov.kprojekt.util.MapOps._

    val subtopology = new SubtopologyBlock(st.name.toInt)
    val (nodes, succ, pred) = st.nodes.foldLeft((Map.empty[String, NodeBlock], Map.empty[String, List[String]], Map.empty[String, List[String]])) { (acc, n) =>
      val (nodes, succ, pred) = acc
      n match {
        case SourceRef(name, topics, next)          => (nodes + (name -> new SourceBlock(name, topics)), succ |+| Map(name -> next.toList), pred)
        case ProcessorRef(name, stores, next, prev) => (nodes + (name -> new ProcessorBlock(name, stores)), succ |+| Map(name -> next.toList), pred |+| Map(name -> prev.toList))
        case SinkRef(name, topic, prev)             => (nodes + (name -> new SinkBlock(name, topic)), succ, pred |+| Map(name -> prev.toList))
      }
    }

    subtopology.addNodes(nodes.values.toSeq)

    nodes.foreach { case (nodeName, node) =>
      val ps = extractNodes(nodeName, pred, nodes)
      val ss = extractNodes(nodeName, succ, nodes)

      node.addPredecessors(ps)
      node.addSuccessors(ss)
    }

    subtopology
  }

  private def extractNodes(nodeName: String, m: Map[String, List[String]], nodes: Map[String, NodeBlock]): List[Node] =
    m.getOrElse(nodeName, List.empty[String])
      .foldLeft(List.empty[Node]) { (acc, name) =>
        nodes.get(name) match {
          case Some(node) =>
            acc :+ node
          case None => acc
        }
      }
}
