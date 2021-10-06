package com.github.gchudnov.kprojekt.naming

import zio.{ Has, UIO, ZIO, ZLayer }

import scala.util.matching.Regex

/**
 * Node naming
 */
final class LiveNamer(config: NamerConfig) extends Namer {
  import LiveNamer._

  override def name(input: String): UIO[NodeName] =
    UIO.succeed(get(input))

  def get(input: String): NodeName =
    pattern
      .findFirstMatchIn(input)
      .map { m =>
        val operator = Option(m.group(RxGroups.Operator)).getOrElse("")
        val uid      = Option(m.group(RxGroups.Uid)).getOrElse("")
        val suffix   = Option(m.group(RxGroups.Suffix)).getOrElse("")

        val id    = makeId(uid)
        val alias = makeAlias(suffix, operator, id)

        NodeName(id = id, alias = alias, originalName = input)
      }
      .getOrElse(NodeName(id = None, alias = input, originalName = input))

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

  private def makeAlias(suffix: String, operator: String, id: Option[Int]): String = {
    val value = if (suffix.nonEmpty) suffix else operator
    if (id.isDefined)
      shortenedAlias(value)
    else
      value
  }
}

object LiveNamer {

  object RxGroups {
    val Kind     = "kind"
    val Operator = "operator"
    val Uid      = "uid"
    val Suffix   = "suffix"
  }

  def layer: ZLayer[Has[NamerConfig], Nothing, Has[Namer]] =
    (for {
      config <- ZIO.service[NamerConfig]
      service = new LiveNamer(config)
    } yield service).toLayer

  private val groupNames = Seq(RxGroups.Kind, RxGroups.Operator, RxGroups.Uid, RxGroups.Suffix)
  private val pattern    = new Regex("""^(?<kind>\w+)-(?<operator>[\w-]+)-(?<uid>\d+)-?(?<suffix>\w+)?$""", groupNames: _*)

  private[naming] def makeId(uid: String): Option[Int] =
    if (uid.nonEmpty)
      Some(Integer.parseInt(uid))
    else
      None
}
