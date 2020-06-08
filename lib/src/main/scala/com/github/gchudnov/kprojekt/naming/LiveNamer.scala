package com.github.gchudnov.kprojekt.naming

import zio.UIO

import scala.util.matching.Regex

/**
 * Node naming
 */
final class LiveNamer(config: NameConfig) extends Namer.Service {
  import LiveNamer._

  override def name(name: String): UIO[NodeName] =
    UIO.succeed {
      pattern
        .findFirstMatchIn(name)
        .map { m =>
          val operator = Option(m.group(Parts.Operator)).getOrElse("")
          val uid      = Option(m.group(Parts.Uid)).getOrElse("")
          val suffix   = Option(m.group(Parts.Suffix)).getOrElse("")

          val id    = buildId(uid)
          val alias = buildAlias(suffix, operator, id)

          NodeName(id = id, alias = alias, original = name)
        }
        .getOrElse(NodeName(name))
    }

  private def shortenedAlias(alias: String): String = {

    def len(xs: Seq[String]): Int =
      xs.foldLeft(0)((acc, x) => acc + x.length)

    @scala.annotation.tailrec
    def iterate(acc: List[String], rest: List[String]): List[String] =
      rest match {
        case h :: tail if len(acc ++ rest) > config.maxLenWithoutShortening =>
          iterate(acc :+ h.charAt(0).toString, tail)
        case _ =>
          acc ++ rest
      }

    val input = alias.split("""[\s-_]""").toList
    iterate(List.empty[String], input).mkString(config.separator)
  }

  private def buildAlias(suffix: String, operator: String, id: Option[Int]): String = {
    val value = if (suffix.nonEmpty) suffix else operator
    if (id.isDefined)
      shortenedAlias(value)
    else
      value
  }
}

object LiveNamer {
  object Parts {
    val Kind     = "kind"
    val Operator = "operator"
    val Uid      = "uid"
    val Suffix   = "suffix"
  }

  private val parts = Seq(Parts.Kind, Parts.Operator, Parts.Uid, Parts.Suffix)

  private val pattern = new Regex("""^(?<kind>\w+)-(?<operator>[\w-]+)-(?<uid>\d+)-?(?<suffix>\w+)?$""", parts: _*)

  private[naming] def buildId(uid: String): Option[Int] =
    if (uid.nonEmpty)
      Some(Integer.parseInt(uid))
    else
      None
}
