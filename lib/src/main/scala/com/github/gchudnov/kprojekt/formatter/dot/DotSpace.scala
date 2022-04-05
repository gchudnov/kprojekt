package com.github.gchudnov.kprojekt.formatter.dot

trait DotSpace

object DotSpace {
  case object Small  extends DotSpace
  case object Medium extends DotSpace
  case object Large  extends DotSpace

  def parse(value: String): Either[Throwable, DotSpace] =
    value.toLowerCase() match {
      case "s" | "small"  => Right(Small)
      case "m" | "medium" => Right(Medium)
      case "l" | "large"  => Right(Large)
      case value          => Left(new RuntimeException(s"""Unknown space value: '$value', must be one of: [${allowed.mkString(",")}]"""))
    }

  def asString(value: DotSpace): String =
    toName(value)

  private lazy val allowed = List("s", "small", "m", "medium", "l", "large")

  private val toName: Map[DotSpace, String] =
    Map(
      Small  -> "small",  // s
      Medium -> "medium", // m
      Large  -> "large"   // l
    )
}
