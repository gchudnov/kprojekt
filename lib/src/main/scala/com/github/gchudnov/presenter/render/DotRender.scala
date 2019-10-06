package com.github.gchudnov.presenter.render

import cats._
import cats.implicits._

/**
  * Render Topology for GraphViz (Dot-Format)
  * http://www.graphviz.org/
  *
  * cat graph.dot | dot -Tpng > graph.png
  */
case class Dot(value: String, excludes: Set[String] = Set.empty[String])

case class DotRender(inner: Dot) extends Render[Dot] {
  import DotRender._
  import DotInstances.dotMonoid

  override def get: Dot = inner

  override def topologyStart(name: String): Render[Dot] = DotRender(inner |+| Dot(s"""digraph g_${toId(name)} {\n"""))

  override def topologyEnd(): Render[Dot] = DotRender(inner |+| Dot("}\n"))

  override def topic(name: String): Render[Dot] = DotRender(inner |+| Dot(s"""${toId(name)} [shape=box, label="", xlabel="${name}"];\n"""))

  override def subtopologyStart(name: String): Render[Dot] =
    DotRender(
      inner |+| Dot(
        new StringBuilder()
          .append(s"subgraph cluster_${toId(name)} {\n")
          .append("style=dotted;\n")
          .toString()
      )
    )

  override def subtopologyEnd(): Render[Dot] = DotRender(inner |+| Dot("}\n"))

  override def edge(fromName: String, toName: String): Render[Dot] =
    ifNotExcluded(fromName, toName)(
      DotRender(inner |+| Dot(s"${toId(fromName)} -> ${toId(toName)};\n"))
    )

  override def source(name: String, topics: Seq[String]): Render[Dot] =
    DotRender(
      inner |+|
        Dot(
          new StringBuilder()
            .append(s"""${toId(name)} [shape=ellipse, label="", xlabel="${toLabel(name)}"];\n""")
            .toString()
        )
    )

  override def processor(name: String, stores: Seq[String]): Render[Dot] = {
    val (text, excludes) = if (stores.size == 1) {
      val label = s"""${toId(name)} [shape=ellipse, image="cylinder.png", imagescale=true, fixedsize=true, label="", xlabel="${toLabel(name)}\\n${stores(0)}"];\n"""
      (label, Set(stores(0)))
    } else {
      val label = s"""${toId(name)} [shape=ellipse, label="", xlabel="${toLabel(name)}"];\n"""
      (label, Set.empty[String])
    }

    DotRender(
      inner |+|
        Dot(
          new StringBuilder()
            .append(text)
            .toString(),
          excludes
        )
    )
  }

  override def sink(name: String, topic: String): Render[Dot] =
    DotRender(
      inner |+|
        Dot(
          new StringBuilder()
            .append(s"""${toId(name)} [shape=ellipse, label="", xlabel="${toLabel(name)}"];\n""")
            .toString()
        )
    )

  override def store(name: String): Render[Dot] =
    ifNotExcluded(name)(
      DotRender(
        inner |+|
          Dot(
            new StringBuilder()
              .append(s"""${toId(name)} [shape=cylinder, label="", xlabel="${toLabel(name)}"];\n""")
              .toString()
          )
      )
    )

  override def rank(name1: String, name2: String): Render[Dot] =
    ifNotExcluded(name1, name2)(
      DotRender(
        inner |+|
          Dot(
            new StringBuilder()
              .append(s"""{ rank=same; ${toId(name1)}; ${toId(name2)}; };\n""")
              .toString()
          )
      )
    )

  private def ifNotExcluded(names: String*)(r: Render[Dot]): Render[Dot] = {
    if (names.intersect(inner.excludes.toSeq).nonEmpty) {
      this
    } else {
      r
    }
  }
}

object DotRender {
  import DotInstances.dotMonoid

  def apply() = new DotRender(Monoid[Dot].empty)

  def toId(name: String): String = {
    name.replace("-", "_")
  }

  def toLabel(name: String): String = {
    val parts = name.split("-")
    if (parts.size == 3) {
      parts(1)
    } else if (parts.size == 4) {
      parts(1) + "-" + parts(2)
    } else {
      name
    }
  }
}

sealed trait DotInstances {

  implicit val dotShow: Show[Dot] = Show.show[Dot] { dot =>
    dot.value
  }

  implicit val dotMonoid: Monoid[Dot] = new Monoid[Dot] {
    override def empty: Dot = Dot("")
    override def combine(x: Dot, y: Dot): Dot = Dot(x.value + y.value, x.excludes ++ y.excludes)
  }

  implicit val dotRender: Render[Dot] = DotRender()

}

object DotInstances extends DotInstances
