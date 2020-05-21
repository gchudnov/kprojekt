package com.github.gchudnov.kprojekt.formatter.dot

import cats.implicits._
import com.github.gchudnov.kprojekt.formatter.Encoder

/**
 * Format Topology for GraphViz (Dot-Format)
 * http://www.graphviz.org/
 *
 * cat graph.dot | dot -Tpng > graph.png
 */
final case class DotFormatState(storesToEmbed: Set[String] = Set.empty[String], indent: Int = 0)

final case class DotFormat(inner: String, state: DotFormatState = DotFormatState()) extends Encoder[Dot] {
  import DotFormat._

  override def toString: String = inner

  override def topologyStart(name: String): Encoder[Dot] =
    DotFormat(
      inner |+| (
        new StringBuilder()
          .append(s"""${T}digraph g_${toId(name)} {\n""")
          .append(s"""${T2}graph [fontname = "${defaultFontName}", fontsize=${defaultFontSize}];\n""")
          .append(s"""${T2}node [fontname = "${defaultFontName}", fontsize=${defaultFontSize}];\n""")
          .append(s"""${T2}edge [fontname = "${defaultFontName}", fontsize=${defaultFontSize}];\n""")
          .toString()
        ),
      state.copy(indent = state.indent + 1)
    )

  override def topologyEnd(): Encoder[Dot] =
    DotFormat(
      inner |+| (
        s"""${T_1}}\n"""
      ),
      state.copy(indent = state.indent - 1)
    )

  override def topic(name: String): Encoder[Dot] =
    DotFormat(
      inner |+| (
        s"""${T}${toId(name)} [shape=box, fixedsize=true, label="${name}", xlabel=""];\n"""
      ),
      state
    )

  override def subtopologyStart(name: String): Encoder[Dot] =
    DotFormat(
      inner |+| (
        new StringBuilder()
          .append(s"${T}subgraph cluster_${toId(name)} {\n")
          .append(s"${T}${T}style=dotted;\n")
          .toString()
        ),
      state.copy(indent = state.indent + 1)
    )

  override def subtopologyEnd(): Encoder[Dot] =
    DotFormat(
      inner |+| (
        s"""${T_1}}\n"""
      ),
      state.copy(indent = state.indent - 1)
    )

  override def edge(fromName: String, toName: String): Encoder[Dot] =
    ifNotEmbedded(fromName, toName)(
      DotFormat(
        inner |+| (
          s"${T}${toId(fromName)} -> ${toId(toName)};\n"
        ),
        state
      )
    )

  override def source(name: String, topics: Seq[String]): Encoder[Dot] =
    DotFormat(
      inner |+|
        (
          new StringBuilder()
            .append(s"""${T}${toId(name)} [shape=ellipse, fixedsize=true, label="${toLabel(name)}", xlabel=""];\n""")
            .toString()
          ),
      state
    )

  override def processor(name: String, stores: Seq[String]): Encoder[Dot] = {
    val text =
      if (stores.size == 1 && state.storesToEmbed.contains(stores.head))
        s"""${T}${toId(name)} [shape=ellipse, image="${DotConfig.CylinderFileName}", imagescale=true, fixedsize=true, label="", xlabel="${toLabel(name)}\n(${toLabel(
          stores(0)
        )})"];\n"""
      else
        s"""${T}${toId(name)} [shape=ellipse, fixedsize=true, label="${toLabel(name)}", xlabel=""];\n"""

    DotFormat(
      inner |+|
        (
          new StringBuilder()
            .append(text)
            .toString()
          ),
      state
    )
  }

  override def sink(name: String, topic: String): Encoder[Dot] =
    DotFormat(
      inner |+|
        (
          new StringBuilder()
            .append(s"""${T}${toId(name)} [shape=ellipse, fixedsize=true, label="${toLabel(name)}", xlabel=""];\n""")
            .toString()
          ),
      state
    )

  /**
   * used to plan the embeddment of stores into the graph node
   * A store `s` can be embedded if:
   * - processor references only 1 store
   * - there are no other references to this store
   * That means dfs(s) = 1
   */
  override def storeEdges(edges: Seq[(String, String)]): Encoder[Dot] =
    DotFormat(
      inner,
      state.copy(storesToEmbed = DotFormat.findStoresToEmbed(edges))
    )

  override def store(name: String): Encoder[Dot] =
    ifNotEmbedded(name)(
      DotFormat(
        inner |+|
          (
            new StringBuilder()
              .append(s"""${T}${toId(name)} [shape=cylinder, fixedsize=false, width=0.5, label="", xlabel="${toLabel(name)}"];\n""")
              .toString()
            ),
        state
      )
    )

  override def rank(name1: String, name2: String): Encoder[Dot] =
    ifNotEmbedded(name1, name2)(
      DotFormat(
        inner |+|
          (
            new StringBuilder()
              .append(s"""${T}{ rank=same; ${toId(name1)}; ${toId(name2)}; };\n""")
              .toString()
            ),
        state
      )
    )

  private def ifNotEmbedded(names: String*)(r: Encoder[Dot]): Encoder[Dot] =
    if (names.intersect(state.storesToEmbed.toSeq).nonEmpty)
      this
    else
      r

  private def T: String   = indent(state.indent)
  private def T2: String  = indent(state.indent + 1)
  private def T_1: String = indent(state.indent - 1)

  private def indent(value: Int): String = " " * (value * defaultIndent)
}

object DotFormat {

  private val defaultIndent   = 2
  private val defaultFontName = "sans-serif"
  private val defaultFontSize = "10"

  def apply() = new DotFormat("")

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
