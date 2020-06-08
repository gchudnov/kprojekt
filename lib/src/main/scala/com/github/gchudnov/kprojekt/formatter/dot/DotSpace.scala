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
      case value          => Left(new RuntimeException(s"""unknown space value: '${value}'. Allowed values are: [${allowed.mkString(",")}]"""))
    }

  private lazy val allowed = List("s", "small", "m", "medium", "l", "large")
}
