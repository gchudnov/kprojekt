package com.github.gchudnov.kprojekt.formatter

import scala.util.matching.Regex

/**
 * Represents a node name, like: KSTREAM-MAPVALUES-0000000002 or KSTREAM-SELECT-KEY-0000000002
 */
final case class NodeName(kind: String, label: String, uid: String)

object NodeName {

  private val pattern = new Regex("""^(?<kind>\w+)-(?<label>[\w-]+)-(?<uid>\d+)$""")

  def parse(name: String): NodeName =
    pattern
      .findFirstMatchIn(name)
      .map(m => NodeName(m.group("kind"), m.group("label"), m.group("uid")))
      .getOrElse(NodeName("", name, ""))

}
