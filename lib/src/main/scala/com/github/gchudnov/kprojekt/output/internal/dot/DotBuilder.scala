package com.github.gchudnov.kprojekt.output.internal.dot

import com.github.gchudnov.kprojekt.output.Id
import com.github.gchudnov.kprojekt.output.Edge
import com.github.gchudnov.kprojekt.output.Builder
import com.github.gchudnov.kprojekt.formatter.dot.DotConfig
import com.github.gchudnov.kprojekt.output.internal.dot.DotBuilder.State

/**
  * Dot Builder
  *
  * http://www.graphviz.org/
  */
private[internal] final class DotBuilder(config: DotConfig, state: State) extends Builder {
  import DotBuilder._

  override def build: String = 
    state.sb.toString()

  override def legend(ids: Iterable[Id]): Builder = {
    val l = ids.map(id => (id -> toLegendEntry(id))).toMap

    withState(state.copy(legend = l))
  }

  override def topologyStart(name: String): Builder = {
    val sb1 = new StringBuilder()
      .append(s"""${T1}digraph g_${sanitizeName(name)} {\n""")
      .append(s"""${T2}pack="true"\n""")
      .append(s"""${T2}packmode="clust"\n""")
      .append(s"""${T2}graph [fontname = "${config.fontName}", fontsize=${config.fontSize}, pad="$padG", nodesep="$sepNode", ranksep="$sepRank"];\n""")
      .append(s"""${T2}node [fontname = "${config.fontName}", fontsize=${config.fontSize}];\n""")
      .append(s"""${T2}edge [fontname = "${config.fontName}", fontsize=${config.fontSize}];\n""")

      withState(state.copy(sb = state.sb.append(sb1), indent = state.indent + 1))
  }

  override def topologyEnd(): Builder = {
    val sb1 = new StringBuilder()
      .append(buildLegend())
      .append(s"""$T_1}\n""")

    withState(state.copy(sb = state.sb.append(sb1), indent = state.indent - 1))
  }

/**

  */

  override def topic(id: Id): Builder = 
    ???

  override def subtopologyStart(name: String): Builder = 
    ???

  override def subtopologyEnd(): Builder = 
    ???

  override def edge(e: Edge): Builder =
    ???

  override def source(id: Id, topics: Iterable[Id]): Builder =
    ???

  override def processor(id: Id, stores: Iterable[Id]): Builder =
    ???

  override def sink(id: Id, topic: Id): Builder =
    ???

  override def storeEdges(edges: Iterable[Edge]): Builder =
    ???

  override def store(id: Id): Builder =
    ???

  override def repository(ns: Iterable[Id]): Builder =
    ???

  override def rank(a: Id, b: Id): Builder =
    ???

  private def buildLegend(): StringBuilder = {
    val sb = new StringBuilder()
    sb.append(s"${T1}subgraph legend_0 {\n")

    sb.append(s"${T2}legend_root [shape=none, margin=0, label=<\n")
    sb.append(s"""$T3<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">\n""")

    sb.append(s"""$T4<TR>\n""")
    sb.append(s"""$T5<TD bgcolor="$colorTableHeaderBg">#</TD>\n""")
    sb.append(s"""$T5<TD bgcolor="$colorTableHeaderBg" align="left">Alias</TD>\n""")
    sb.append(s"""$T5<TD bgcolor="$colorTableHeaderBg" align="left">Name</TD>\n""")
    sb.append(s"""$T4</TR>\n""")

    val table = state.legend.values.toList.sortBy(it => (it.id))

    table.foreach { case LegendEntry(id, alias, n) =>
      sb.append(s"""$T4<TR>\n""")
      sb.append(s"""$T5<TD>${n.getOrElse("")}</TD>\n""")
      sb.append(s"""$T5<TD align="left">$alias</TD>\n""")
      sb.append(s"""$T5<TD align="left">${id.name}</TD>\n""")
      sb.append(s"""$T4</TR>\n""")
    }

    sb.append(s"""$T3</TABLE>\n""")
    sb.append(s"$T2>];\n")
    sb.append(s"$T1}\n")

    sb
  }

  private val padG: String = "0.5"
  private val sepNode: String = "0.5"
  private val sepRank: String = "0.75"

  private val colorTableHeaderBg = "#cdcdcd"

  /**
    * Make a new builder with an updated state
    *
    * @param state
    */
  private def withState(state: State): DotBuilder =
    new DotBuilder(
      config = config,
      state = state
    )

  private def T1: String = 
    tab(state.indent)

  private def T2: String = 
    tab(state.indent + 1)

  private def T3: String  = 
    tab(state.indent + 2)

  private def T4: String  = 
    tab(state.indent + 3)

  private def T5: String  = 
    tab(state.indent + 4)

  private def T_1: String = 
    tab(state.indent - 1)

  private def tab(value: Int): String = 
    " " * (value * config.indent)
}

object DotBuilder {

  final case class LegendEntry(id: Id, alias: String, n: Option[Int])

  final case class State(
    sb: StringBuilder,
    legend: Map[Id, LegendEntry],
    indent: Int
  )

  object State {
    def empty: State = 
      State(
        sb = new StringBuilder,
        legend = Map.empty[Id, LegendEntry],
        indent = 0
      )
  }

  def apply(): DotBuilder = {
    val config = DotConfig.default
    val state = State.empty

    new DotBuilder(config = config, state = state)
  }

  private def sanitizeName(name: String): String =
    name.replaceAll("""[-.:]""", "_")

  private def toLegendEntry(id: Id): LegendEntry = {
    val (alias, n) = DotLegend.buildEntry(id)

    LegendEntry(id = id, alias = alias, n = n)
  }
}
