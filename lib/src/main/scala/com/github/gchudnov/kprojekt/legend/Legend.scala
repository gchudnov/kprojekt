package com.github.gchudnov.kprojekt.legend

object Legend {

  def build(names: Seq[String]): Map[String, String] =
    names.map(name => (name, alias(name))).toMap

  def alias(name: String): String =
    "A_" + name.toLowerCase()
}
