package com.github.gchudnov.kprojekt.formatter.dot

import cats.implicits._
import com.github.gchudnov.kprojekt.formatter.Folder

final case class DotFolderState(
  inner: String = "",
  legend: Map[String, String] = Map.empty[String, String],
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

  override def toString: String = state.inner

  override def topologyStart(name: String): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(
        inner = state.inner |+| (
          new StringBuilder()
            .append(s"""${T}digraph g_${toId(name)} {\n""")
            .append(s"""${T2}graph [fontname = "${config.fontName}", fontsize=${config.fontSize}];\n""")
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
        inner = state.inner |+| (
          s"""${T_1}}\n"""
        ),
        indent = state.indent - 1
      )
    )

  override def topic(name: String): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(inner =
        state.inner |+| (
          s"""${T}${toId(name)} [shape=box, fixedsize=true, label="${name}", xlabel=""];\n"""
        )
      )
    )

  override def subtopologyStart(name: String): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(
        inner = state.inner |+| (
          new StringBuilder()
            .append(s"${T}subgraph cluster_${toId(name)} {\n")
            .append(s"${T}${T}style=dotted;\n")
            .toString()
          ),
        indent = state.indent + 1
      )
    )

  override def subtopologyEnd(): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(
        inner = state.inner |+| (
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
          state.inner |+| (
            s"${T}${toId(fromName)} -> ${toId(toName)};\n"
          )
        )
      )
    )

  override def source(name: String, topics: Seq[String]): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(inner =
        state.inner |+|
          (
            new StringBuilder()
              .append(s"""${T}${toId(name)} [shape=ellipse, fixedsize=true, label="${toLabel(name)}", xlabel=""];\n""")
              .toString()
            )
      )
    )

  override def processor(name: String, stores: Seq[String]): DotFolder = {
    val text =
      if (stores.size == 1 && state.storesToEmbed.contains(stores.head) && config.isEmbedStore) {
        val label = s"${toLabel(name)}\n(${toLabel(stores.head)})"
        s"""${T}${toId(name)} [shape=ellipse, image="${DotConfig.cylinderFileName}", imagescale=true, fixedsize=true, label="${label}", xlabel=""];\n"""
      } else
        s"""${T}${toId(name)} [shape=ellipse, fixedsize=true, label="${toLabel(name)}", xlabel=""];\n"""

    new DotFolder(
      config = config,
      state = state.copy(inner =
        state.inner |+|
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
        state.inner |+|
          (
            new StringBuilder()
              .append(s"""${T}${toId(name)} [shape=ellipse, fixedsize=true, label="${toLabel(name)}", xlabel=""];\n""")
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
          state.inner |+|
            (
              new StringBuilder()
                .append(s"""${T}${toId(name)} [shape=cylinder, fixedsize=true, width=0.5, label="${toLabel(name)}", xlabel=""];\n""")
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
          state.inner |+|
            (
              new StringBuilder()
                .append(s"""${T}{ rank=same; ${toId(name1)}; ${toId(name2)}; };\n""")
                .toString()
              )
        )
      )
    )

  override def legend(ns: Map[String, String]): DotFolder =
    new DotFolder(
      config = config,
      state = state.copy(legend = ns)
    )

  private def ifNotEmbedded(names: String*)(r: => DotFolder): DotFolder =
    if (config.isEmbedStore && names.intersect(state.storesToEmbed.toSeq).nonEmpty)
      this
    else
      r

  private def T: String   = indent(state.indent)
  private def T2: String  = indent(state.indent + 1)
  private def T_1: String = indent(state.indent - 1)

  private def indent(value: Int): String = " " * (value * config.indent)
}

object DotFolder {

  def toId(name: String): String =
    name.replaceAll("""[-.]""", "_")

  def toLabel(name: String): String =
    DotNodeName.parse(name).label

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
