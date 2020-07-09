package com.github.gchudnov.kprojekt.formatter.dot

import com.github.gchudnov.kprojekt.formatter.Folder
import com.github.gchudnov.kprojekt.formatter.dot.DotSpace._
import com.github.gchudnov.kprojekt.naming.{ Legend, LegendEntry }

final case class DotFolderState(
  inner: String = "",
  legend: Legend = Legend.empty,
  storesToEmbed: Set[String] = Set.empty[String],
  indent: Int = 0
)

/**
 * Fold Topology for GraphViz (Dot-Format)
 * http://www.graphviz.org/
 *
 * cat graph.dot | dot -Tpng > graph.png
 */
final class DotFolder(config: DotConfig, state: DotFolderState = DotFolderState()) extends Folder.Service {
  import DotFolder._
  import DotColors._

  override def toString: String = state.inner

  override def topologyStart(name: String): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(
        inner = state.inner + (
          new StringBuilder()
            .append(s"""${T1}digraph g_${toId(name)} {\n""")
            .append(s"""${T2}pack="true"\n""")
            .append(s"""${T2}packmode="clust"\n""")
            .append(s"""${T2}graph [fontname = "${config.fontName}", fontsize=${config.fontSize}, pad="${gpad}", nodesep="${nodesep}", ranksep="${ranksep}"];\n""")
            .append(s"""${T2}node [fontname = "${config.fontName}", fontsize=${config.fontSize}];\n""")
            .append(s"""${T2}edge [fontname = "${config.fontName}", fontsize=${config.fontSize}];\n""")
            .toString()
          ),
        indent = state.indent + 1
      )
    )

  override def topologyEnd(): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(
        inner = state.inner + (
          new StringBuilder()
            .append(withLegend())
            .append(s"""${T_1}}\n""")
            .toString()
          ),
        indent = state.indent - 1
      )
    )

  override def topic(name: String): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(inner =
        state.inner + (
          s"""${T1}${toId(name)} [shape=box, fixedsize=true, label="${alias(name)}", xlabel="", style=filled, fillcolor="${FillColorTopic}"];\n"""
        )
      )
    )

  override def subtopologyStart(name: String): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(
        inner = state.inner + (
          new StringBuilder()
            .append(s"${T1}subgraph cluster_${toId(name)} {\n")
            .append(s"${T2}style=dotted;\n")
            .toString()
          ),
        indent = state.indent + 1
      )
    )

  override def subtopologyEnd(): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(
        inner = state.inner + (
          s"""${T_1}}\n"""
        ),
        indent = state.indent - 1
      )
    )

  override def edge(fromName: String, toName: String): DotFolder =
    ifNotEmbedded(fromName, toName)(
      new DotFolder(
        config = config,
        state = state.copy(inner =
          state.inner + (
            s"${T1}${toId(fromName)} -> ${toId(toName)};\n"
          )
        )
      )
    )

  override def source(name: String, topics: Seq[String]): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(inner =
        state.inner +
          (
            new StringBuilder()
              .append(s"""${T1}${toId(name)} [shape=ellipse, fixedsize=true, label="${alias(name)}", xlabel=""];\n""")
              .toString()
            )
      )
    )

  override def processor(name: String, stores: Seq[String]): DotFolder = {
    val text =
      if (stores.size == 1 && state.storesToEmbed.contains(stores.head) && config.isEmbedStore) {
        val label = s"""${alias(name)}\\n(${alias(stores.head)})"""
        s"""${T1}${toId(name)} [shape=ellipse, image="${DotConfig.cylinderFileName}", imagescale=true, fixedsize=true, label="${label}", xlabel=""];\n"""
      } else
        s"""${T1}${toId(name)} [shape=ellipse, fixedsize=true, label="${alias(name)}", xlabel=""];\n"""

    new DotFolder(
      config = config,
      state = state.copy(inner =
        state.inner +
          (
            new StringBuilder()
              .append(text)
              .toString()
            )
      )
    )
  }

  override def sink(name: String, topic: String): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(inner =
        state.inner +
          (
            new StringBuilder()
              .append(s"""${T1}${toId(name)} [shape=ellipse, fixedsize=true, label="${alias(name)}", xlabel=""];\n""")
              .toString()
            )
      )
    )

  /**
   * used to plan the embedding of stores into the graph node.
   * A store `s` can be embedded if:
   * - processor references only 1 store
   * - there are no other references to this store
   * That means dfs(s) = 1
   */
  override def storeEdges(edges: Seq[(String, String)]): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(storesToEmbed = DotFolder.findStoresToEmbed(edges))
    )

  override def store(name: String): DotFolder =
    ifNotEmbedded(name)(
      new DotFolder(
        config = config,
        state = state.copy(inner =
          state.inner +
            (
              new StringBuilder()
                .append(s"""${T1}${toId(name)} [shape=cylinder, fixedsize=true, width=0.5, label="${alias(name)}", xlabel="", style=filled, fillcolor="${FillColorStore}"];\n""")
                .toString()
              )
        )
      )
    )

  override def rank(name1: String, name2: String): DotFolder =
    ifNotEmbedded(name1, name2)(
      new DotFolder(
        config = config,
        state = state.copy(inner =
          state.inner +
            (
              new StringBuilder()
                .append(s"""${T1}{ rank=same; ${toId(name1)}; ${toId(name2)}; };\n""")
                .toString()
              )
        )
      )
    )

  override def legend(l: Legend): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(legend = l)
    )

  private def withLegend(): String =
    if (!config.hasLegend)
      ""
    else {
      val sb = new StringBuilder()
      sb.append(s"${T1}subgraph legend_0 {\n")

      sb.append(s"${T2}legend_root [shape=none, margin=0, label=<\n")
      sb.append(s"""${T3}<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">\n""")

      sb.append(s"""${T4}<TR>\n""")
      sb.append(s"""${T5}<TD bgcolor="${FillColorTableHeader}">#</TD>\n""")
      sb.append(s"""${T5}<TD bgcolor="${FillColorTableHeader}" align="left">Alias</TD>\n""")
      sb.append(s"""${T5}<TD bgcolor="${FillColorTableHeader}" align="left">Name</TD>\n""")
      sb.append(s"""${T4}</TR>\n""")

      state.legend.table.foreach {
        case LegendEntry(id, k, v) =>
          sb.append(s"""${T4}<TR>\n""")
          sb.append(s"""${T5}<TD>${id.getOrElse("")}</TD>\n""")
          sb.append(s"""${T5}<TD align="left">${k}</TD>\n""")
          sb.append(s"""${T5}<TD align="left">${v}</TD>\n""")
          sb.append(s"""${T4}</TR>\n""")
      }

      sb.append(s"""${T3}</TABLE>\n""")
      sb.append(s"${T2}>];\n")
      sb.append(s"${T1}}\n")

      sb.toString()
    }

  private def ifNotEmbedded(names: String*)(r: => DotFolder): DotFolder =
    if (config.isEmbedStore && names.intersect(state.storesToEmbed.toSeq).nonEmpty)
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

  private def alias(name: String): String =
    state.legend
      .nodeName(name)
      .map { nn =>
        nn.id.map(i => s"""${nn.alias}\\n${i}""").getOrElse(s"${nn.alias}")
      }
      .getOrElse(DotFolder.UnknownName)

  private val gpad: String = "0.5"

  private val nodesep: String = config.space match {
    case Small  => "0.25"
    case Medium => "0.5"
    case Large  => "1.0"
  }

  private val ranksep: String = config.space match {
    case Small  => "0.5"
    case Medium => "0.75"
    case Large  => "1.0"
  }
}

object DotFolder {
  import com.github.gchudnov.kprojekt.util.MapOps._

  val UnknownName = "?"

  def toId(name: String): String =
    name.replaceAll("""[-.]""", "_")

  def findStoresToEmbed(storeEdges: Seq[(String, String)]): Set[String] = {
    val (stores, adjList) = storeEdges.foldLeft((Set.empty[String], Map.empty[String, List[String]])) { (acc, e) =>
      val (stores, adjList) = acc
      (stores + e._2, adjList |+| Map(e._1 -> List(e._2)) |+| Map(e._2 -> List(e._1)))
    }

    stores.foldLeft(Set.empty[String]) { (acc, s) =>
      val as = adjList.getOrElse(s, List.empty[String])
      if (as.size > 1)
        acc
      else
        acc + s
    }
  }
}
