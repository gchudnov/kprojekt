package com.github.gchudnov.kprojekt.output.internal.dot

import com.github.gchudnov.kprojekt.output.Id
import scala.util.matching.Regex
import scala.util.control.Exception._

object DotLegend {

  private object rxGroups {
    val kind       = "kind"
    val operator   = "operator"
    val identifier = "identifier"
    val suffix     = "suffix"
  }

  private val rxGroupNames = List(rxGroups.kind, rxGroups.operator, rxGroups.identifier, rxGroups.suffix)
  private val rxPattern    = new Regex("""^(?<kind>\w+)-(?<operator>[\w-]+)-(?<identifier>\d+)-?(?<suffix>\w+)?$""", rxGroupNames: _*)

  def buildEntry(id: Id): (String, Option[Int]) =
    rxPattern
      .findFirstMatchIn(id.name)
      .map { m =>
        val operator   = Option(m.group(rxGroups.operator)).getOrElse("")
        val identifier = Option(m.group(rxGroups.identifier)).getOrElse("")
        val suffix     = Option(m.group(rxGroups.suffix)).getOrElse("")

        val n     = toN(identifier)
        val alias = toAlias(operator, suffix, n)

        (alias, n)
      }
      .getOrElse((id.name, None))

  private def toN(value: String): Option[Int] =
    if (value.nonEmpty)
      nonFatalCatch.either(Integer.parseInt(value)).toOption
    else
      None

  private def toAlias(operator: String, suffix: String, n: Option[Int]) = {
    val value = List(operator, suffix).filter(_.nonEmpty).mkString("_")
    val parts = value.split("""[\s-_]""").toList
    parts.mkString(".")
  }
}
