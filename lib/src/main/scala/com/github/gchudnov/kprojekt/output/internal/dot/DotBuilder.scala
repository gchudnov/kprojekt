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
final class DotBuilder(config: DotConfig, state: State) extends Builder {
  import DotBuilder._

  override def build: String = 
    ???

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

  override def topologyEnd(): Builder =
    ???

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

  private val padG: String = "0.5"
  private val sepNode: String = "0.5"
  private val sepRank: String = "0.75"

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

  private def tab(value: Int): String = 
    " " * (value * config.indent)
}

object DotBuilder {

  final case class State(
    sb: StringBuilder,
    indent: Int
  )

  object State {
    def empty: State = 
      State(
        sb = new StringBuilder,
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
}
