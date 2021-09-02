package com.github.gchudnov.kprojekt.formatter.dot

import com.github.gchudnov.kprojekt.formatter.Folder
import com.github.gchudnov.kprojekt.formatter.dot.DotSpace._
import com.github.gchudnov.kprojekt.formatter.dot.legend.Legend
import com.github.gchudnov.kprojekt.ids.NodeId
import com.github.gchudnov.kprojekt.naming.{ Namer, NodeName }

final case class DotFolderState(
  inner: String = "",
  legend: Legend = Legend.empty,
  storesToEmbed: Set[NodeId] = Set.empty[NodeId],
  indent: Int = 0
)

/**
 * Fold Topology for GraphViz (Dot-Format)
 *
 * http://www.graphviz.org/
 *
 * cat graph.dot | dot -Tpng > graph.png
 */
final class DotFolder(config: DotConfig, namer: Namer.Service, state: DotFolderState) extends Folder.Service {
  import DotColors._
  import DotFolder._

  override def toString: String = state.inner

  override def topologyStart(name: String): DotFolder =
    withNewState(
      state.copy(
        inner = state.inner + (
          new StringBuilder()
            .append(s"""${T1}digraph g_${sanitize(name)} {\n""")
            .append(s"""${T2}pack="true"\n""")
            .append(s"""${T2}packmode="clust"\n""")
            .append(s"""${T2}graph [fontname = "${config.fontName}", fontsize=${config.fontSize}, pad="$gpad", nodesep="$nodesep", ranksep="$ranksep"];\n""")
            .append(s"""${T2}node [fontname = "${config.fontName}", fontsize=${config.fontSize}];\n""")
            .append(s"""${T2}edge [fontname = "${config.fontName}", fontsize=${config.fontSize}];\n""")
            .toString()
        ),
        indent = state.indent + 1
      )
    )

  override def topologyEnd(): DotFolder =
    withNewState(
      state.copy(
        inner = state.inner + (
          new StringBuilder()
            .append(withLegend())
            .append(s"""$T_1}\n""")
            .toString()
        ),
        indent = state.indent - 1
      )
    )

  override def topic(id: NodeId): DotFolder =
    withNewState(
      state.copy(inner =
        state.inner + (
          s"""$T1${toDotId(id)} [shape=box, fixedsize=true, label="${alias(id)}", xlabel="", style=filled, fillcolor="$FillColorTopic"];\n"""
        )
      )
    )

  override def subtopologyStart(name: String): DotFolder =
    withNewState(
      state.copy(
        inner = state.inner + (
          new StringBuilder()
            .append(s"${T1}subgraph cluster_${sanitize(name)} {\n")
            .append(s"${T2}style=dotted;\n")
            .toString()
        ),
        indent = state.indent + 1
      )
    )

  override def subtopologyEnd(): DotFolder =
    withNewState(
      state.copy(
        inner = state.inner + (
          s"""$T_1}\n"""
        ),
        indent = state.indent - 1
      )
    )

  override def edge(from: NodeId, to: NodeId): DotFolder =
    ifNotEmbedded(from, to)(
      withNewState(
        state.copy(inner =
          state.inner + (
            s"$T1${toDotId(from)} -> ${toDotId(to)};\n"
          )
        )
      )
    )

  override def source(id: NodeId, topics: Seq[NodeId]): DotFolder =
    withNewState(
      state.copy(inner =
        state.inner +
          (
            new StringBuilder()
              .append(s"""$T1${toDotId(id)} [shape=ellipse, fixedsize=true, label="${alias(id)}", xlabel=""];\n""")
              .toString()
          )
      )
    )

  override def processor(id: NodeId, stores: Seq[NodeId]): DotFolder = {
    val text =
      if (stores.size == 1 && state.storesToEmbed.contains(stores.head) && config.isEmbedStore) {
        val label = s"""${alias(id)}\\n(${alias(stores.head)})"""
        s"""$T1${toDotId(id)} [shape=ellipse, image="${DotConfig.cylinderFileName}", imagescale=true, fixedsize=true, label="$label", xlabel=""];\n"""
      } else
        s"""$T1${toDotId(id)} [shape=ellipse, fixedsize=true, label="${alias(id)}", xlabel=""];\n"""

    withNewState(
      state.copy(inner =
        state.inner +
          (
            new StringBuilder()
              .append(text)
              .toString()
          )
      )
    )
  }

  override def sink(id: NodeId, topic: NodeId): DotFolder =
    withNewState(
      state.copy(inner =
        state.inner +
          (
            new StringBuilder()
              .append(s"""$T1${toDotId(id)} [shape=ellipse, fixedsize=true, label="${alias(id)}", xlabel=""];\n""")
              .toString()
          )
      )
    )

  /**
   * used to plan the embedding of stores into the graph node.
   * {{{
   * A store `s` can be embedded if:
   * - processor references only 1 store
   * - there are no other references to this store
   * That means dfs(s) = 1
   * }}}
   */
  override def storeEdges(edges: Seq[(NodeId, NodeId)]): DotFolder =
    withNewState(state.copy(storesToEmbed = DotFolder.findStoresToEmbed(edges)))

  override def store(id: NodeId): DotFolder =
    ifNotEmbedded(id)(
      withNewState(
        state.copy(inner =
          state.inner +
            (
              new StringBuilder()
                .append(s"""$T1${toDotId(id)} [shape=cylinder, fixedsize=true, width=0.5, label="${alias(id)}", xlabel="", style=filled, fillcolor="$FillColorStore"];\n""")
                .toString()
            )
        )
      )
    )

  override def rank(a: NodeId, b: NodeId): DotFolder =
    ifNotEmbedded(a, b)(
      withNewState(
        state.copy(inner =
          state.inner +
            (
              new StringBuilder()
                .append(s"""$T1{ rank=same; ${toDotId(a)}; ${toDotId(b)}; };\n""")
                .toString()
            )
        )
      )
    )

  override def repository(ns: Seq[NodeId]): DotFolder =
    withNewState(state.copy(legend = Legend(namer, ns)))

  private def withLegend(): String =
    if (!config.hasLegend)
      ""
    else {
      val sb = new StringBuilder()
      sb.append(s"${T1}subgraph legend_0 {\n")

      sb.append(s"${T2}legend_root [shape=none, margin=0, label=<\n")
      sb.append(s"""$T3<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">\n""")

      sb.append(s"""$T4<TR>\n""")
      sb.append(s"""$T5<TD bgcolor="$FillColorTableHeader">#</TD>\n""")
      sb.append(s"""$T5<TD bgcolor="$FillColorTableHeader" align="left">Alias</TD>\n""")
      sb.append(s"""$T5<TD bgcolor="$FillColorTableHeader" align="left">Name</TD>\n""")
      sb.append(s"""$T4</TR>\n""")

      state.legend.table.foreach { case NodeName(id, alias, originalName) =>
        sb.append(s"""$T4<TR>\n""")
        sb.append(s"""$T5<TD>${id.getOrElse("")}</TD>\n""")
        sb.append(s"""$T5<TD align="left">$alias</TD>\n""")
        sb.append(s"""$T5<TD align="left">$originalName</TD>\n""")
        sb.append(s"""$T4</TR>\n""")
      }

      sb.append(s"""$T3</TABLE>\n""")
      sb.append(s"$T2>];\n")
      sb.append(s"$T1}\n")

      sb.toString()
    }

  private def ifNotEmbedded(ids: NodeId*)(r: => DotFolder): DotFolder =
    if (config.isEmbedStore && ids.intersect(state.storesToEmbed.toSeq).nonEmpty)
      this
    else
      r

  private def T1: String  = indent(state.indent)
  private def T2: String  = indent(state.indent + 1)
  private def T3: String  = indent(state.indent + 2)
  private def T4: String  = indent(state.indent + 3)
  private def T5: String  = indent(state.indent + 4)
  private def T_1: String = indent(state.indent - 1)

  private def indent(value: Int): String = " " * (value * config.indent)

  private def alias(id: NodeId): String =
    state.legend
      .entry(id.tId)
      .map { nn =>
        nn.id.map(i => s"""${nn.alias}\\n$i""").getOrElse(s"${nn.alias}")
      }
      .getOrElse(DotFolder.UnknownName)

  private val gpad: String = "0.5"

  private val nodesep: String = config.space match {
    case Small  => "0.25"
    case Medium => "0.5"
    case Large  => "1.0"
    case x      => sys.error(s"invalid space type: $x")
  }

  private val ranksep: String = config.space match {
    case Small  => "0.5"
    case Medium => "0.75"
    case Large  => "1.0"
    case x      => sys.error(s"invalid space size: $x")
  }

  private def withNewState(state: DotFolderState): DotFolder =
    new DotFolder(
      config = config,
      namer = namer,
      state = state
    )
}

object DotFolder {
  import com.github.gchudnov.kprojekt.util.MapOps._

  val UnknownName = "?"

  def sanitize(value: String): String =
    value.replaceAll("""[-.:]""", "_")

  def toDotId(id: NodeId): String =
    sanitize(id.tId)

  def findStoresToEmbed(storeEdges: Seq[(NodeId, NodeId)]): Set[NodeId] = {
    val (stores, adjList) = storeEdges.foldLeft((Set.empty[NodeId], Map.empty[NodeId, List[NodeId]])) { (acc, e) =>
      val (stores, adjList) = acc
      (stores + e._2, adjList |+| Map(e._1 -> List(e._2)) |+| Map(e._2 -> List(e._1)))
    }

    stores.foldLeft(Set.empty[NodeId]) { (acc, s) =>
      val as = adjList.getOrElse(s, List.empty[NodeId])
      if (as.size > 1)
        acc
      else
        acc + s
    }
  }
}
