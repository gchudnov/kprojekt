package com.github.gchudnov.kprojekt.formatter.dot

import scala.util.matching.Regex

/**
 * Represents a node name, like: KSTREAM-MAPVALUES-0000000002 or KSTREAM-SELECT-KEY-0000000002
 */
final case class DotNodeName(kind: String, label: String, uid: String)

object DotNodeName {

  private val pattern = new Regex("""^(?<kind>\w+)-(?<label>[\w-]+)-(?<uid>\d+)$""")

  def parse(name: String): DotNodeName =
    pattern
      .findFirstMatchIn(name)
      .map(m => DotNodeName(m.group("kind"), m.group("label"), m.group("uid")))
      .getOrElse(DotNodeName("", name, ""))

}
