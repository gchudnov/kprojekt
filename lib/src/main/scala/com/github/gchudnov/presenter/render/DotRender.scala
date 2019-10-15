package com.github.gchudnov.presenter.render

import cats._
import cats.implicits._
import com.github.gchudnov.presenter.name.NodeName

/**
  * Render Topology for GraphViz (Dot-Format)
  * http://www.graphviz.org/
  *
  * cat graph.dot | dot -Tpng > graph.png
  */
final case class Dot(value: String)

final case class DotRenderState(storesToEmbed: Set[String] = Set.empty[String], indent: Int = 0)

final case class DotRender(inner: Dot, state: DotRenderState = DotRenderState()) extends Render[Dot] {
  import DotRender._
  import DotInstances.dotMonoid

  override def get: Dot = inner

  override def topologyStart(name: String): Render[Dot] =
    DotRender(
      inner |+| Dot(
        new StringBuilder()
          .append(s"""${T}digraph g_${toId(name)} {\n""")
          .append(s"""${T2}graph [fontname = "${defaultFontName}", fontsize=${defaultFontSize}];\n""")
          .append(s"""${T2}node [fontname = "${defaultFontName}", fontsize=${defaultFontSize}];\n""")
          .append(s"""${T2}edge [fontname = "${defaultFontName}", fontsize=${defaultFontSize}];\n""")
          .toString()
      ),
      state.copy(indent = state.indent + 1)
    )

  override def topologyEnd(): Render[Dot] =
    DotRender(
      inner |+| Dot(
        s"""${T_1}}\n"""
      ),
      state.copy(indent = state.indent - 1)
    )

  override def topic(name: String): Render[Dot] =
    DotRender(
      inner |+| Dot(
        s"""${T}${toId(name)} [shape=box, fixedsize=true, label="${name}", xlabel=""];\n"""
      ),
      state
    )

  override def subtopologyStart(name: String): Render[Dot] =
    DotRender(
      inner |+| Dot(
        new StringBuilder()
          .append(s"${T}subgraph cluster_${toId(name)} {\n")
          .append(s"${T}${T}style=dotted;\n")
          .toString()
      ),
      state.copy(indent = state.indent + 1)
    )

  override def subtopologyEnd(): Render[Dot] = DotRender(
    inner |+| Dot(
      s"""${T_1}}\n"""
    ),
    state.copy(indent = state.indent - 1)
  )

  override def edge(fromName: String, toName: String): Render[Dot] =
    ifNotEmbedded(fromName, toName)(
      DotRender(
        inner |+| Dot(
          s"${T}${toId(fromName)} -> ${toId(toName)};\n"
        ),
        state
      )
    )

  override def source(name: String, topics: Seq[String]): Render[Dot] =
    DotRender(
      inner |+|
        Dot(
          new StringBuilder()
            .append(s"""${T}${toId(name)} [shape=ellipse, fixedsize=true, label="${toLabel(name)}", xlabel=""];\n""")
            .toString()
        ),
      state
    )

  override def processor(name: String, stores: Seq[String]): Render[Dot] = {
    val text = if (stores.size == 1 && state.storesToEmbed.contains(stores(0))) {
      s"""${T}${toId(name)} [shape=ellipse, image="cylinder.png", imagescale=true, fixedsize=true, label="", xlabel="${toLabel(name)}\n(${toLabel(stores(0))})"];\n"""
    } else {
      s"""${T}${toId(name)} [shape=ellipse, fixedsize=true, label="${toLabel(name)}", xlabel=""];\n"""
    }

    DotRender(
      inner |+|
        Dot(
          new StringBuilder()
            .append(text)
            .toString()
        ),
      state
    )
  }

  override def sink(name: String, topic: String): Render[Dot] =
    DotRender(
      inner |+|
        Dot(
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
  override def storeEdges(edges: Seq[(String, String)]): Render[Dot] = {
    DotRender(
      inner,
      state.copy(storesToEmbed = DotRender.findStoresToEmbed(edges))
    )
  }

  override def store(name: String): Render[Dot] =
    ifNotEmbedded(name)(
      DotRender(
        inner |+|
          Dot(
            new StringBuilder()
              .append(s"""${T}${toId(name)} [shape=cylinder, fixedsize=false, width=0.5, label="", xlabel="${toLabel(name)}"];\n""")
              .toString()
          ),
        state
      )
    )

  override def rank(name1: String, name2: String): Render[Dot] =
    ifNotEmbedded(name1, name2)(
      DotRender(
        inner |+|
          Dot(
            new StringBuilder()
              .append(s"""${T}{ rank=same; ${toId(name1)}; ${toId(name2)}; };\n""")
              .toString()
          ),
        state
      )
    )

  private def ifNotEmbedded(names: String*)(r: Render[Dot]): Render[Dot] = {
    if (names.intersect(state.storesToEmbed.toSeq).nonEmpty) {
      this
    } else {
      r
    }
  }

  private def T: String = indent(state.indent)
  private def T2: String = indent(state.indent + 1)
  private def T_1: String = indent(state.indent - 1)

  private def indent(value: Int): String = " " * (value * defaultIndent)
}

object DotRender {
  import DotInstances.dotMonoid

  private val defaultIndent = 2
  private val defaultFontName = "sans-serif"
  private val defaultFontSize = "10"

  def apply() = new DotRender(Monoid[Dot].empty)

  def toId(name: String): String = {
    name.replace("-", "_")
  }

  def toLabel(name: String): String =
    NodeName.parse(name).label

  def findStoresToEmbed(storeEdges: Seq[(String, String)]): Set[String] = {
    val (stores, adjList) = storeEdges.foldLeft((Set.empty[String], Map.empty[String, List[String]]))((acc, e) => {
      val (stores, adjList) = acc
      (stores + e._2, adjList |+| Map(e._1 -> List(e._2)) |+| Map(e._2 -> List(e._1)))
    })

    stores.foldLeft(Set.empty[String])((acc, s) => {
      val as = adjList.getOrElse(s, List.empty[String])
      if (as.size > 1) {
        acc
      } else {
        acc + s
      }
    })
  }
}

sealed trait DotInstances {

  implicit val dotShow: Show[Dot] = Show.show[Dot] { dot =>
    dot.value
  }

  implicit val dotMonoid: Monoid[Dot] = new Monoid[Dot] {
    override def empty: Dot = Dot("")
    override def combine(x: Dot, y: Dot): Dot = Dot(x.value + y.value)
  }

  implicit val dotRender: Render[Dot] = DotRender()

}

object DotInstances extends DotInstances
