package com.github.gchudnov.presenter.render

import cats._
import cats.implicits._

/**
  * Render Topology for GraphViz (Dot-Format)
  * http://www.graphviz.org/
  *
  * cat graph.dot | dot -Tpng > graph.png
  */
case class Dot(value: String)

case class DotRender(inner: Dot) extends Render[Dot] {
  import DotRender._
  import DotInstances.dotMonoid

  override def get: Dot = inner

  override def topologyStart(name: String): Render[Dot] = DotRender(inner |+| Dot(s"""digraph g_${toId(name)} {\n"""))

  override def topologyEnd(): Render[Dot] = DotRender(inner |+| Dot("}\n"))

  override def topic(t: String): Render[Dot] = DotRender(inner |+| Dot(s"""${toId(t)} [shape=box, label="", xlabel="${t}"];\n"""))

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

  override def edge(fromName: String, toName: String): Render[Dot] = DotRender(inner |+| Dot(s"${toId(fromName)} -> ${toId(toName)};\n"))

  override def source(name: String, topics: Seq[String]): Render[Dot] =
    DotRender(
      inner |+|
        Dot(
          new StringBuilder()
            .append(s"""${toId(name)} [shape=ellipse, label="", xlabel="${toLabel(name)}"];\n""")
            .toString()
        )
    )

  override def processor(name: String, stores: Seq[String]): Render[Dot] =
    DotRender(
      inner |+|
        Dot(
          s"""${toId(name)} [shape=ellipse, label="", xlabel="${toLabel(name)}"];\n"""
          // stores
          //   .foldLeft(new StringBuilder)((acc, t) => {
          //     acc.append(s"${toId(t)} -> ${toId(name)};\n")
          //   })
          //   .append(s"""${toId(name)} [shape=ellipse];\n""")
          //   .toString()
        )
    )

  override def sink(name: String, topic: String): Render[Dot] =
    DotRender(
      inner |+|
        Dot(
          new StringBuilder()
            .append(s"""${toId(name)} [shape=ellipse, label="", xlabel="${toLabel(name)}"];\n""")
            .toString()
        )
    )
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
    override def combine(x: Dot, y: Dot): Dot = Dot(x.value + y.value)
  }

  implicit val dotRender: Render[Dot] = DotRender()

}

object DotInstances extends DotInstances
