package com.github.gchudnov.kprojekt.formatter.dot

import zio.test._
import zio.test.Assertion._

object DotSpaceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("DotSpaceSpec")(
      test("space can be parsed") {
        val cases = Map(
          "s"      -> Right(DotSpace.Small),
          "small"  -> Right(DotSpace.Small),
          "m"      -> Right(DotSpace.Medium),
          "medium" -> Right(DotSpace.Medium),
          "l"      -> Right(DotSpace.Large),
          "large"  -> Right(DotSpace.Large)
        )

        val actual = cases.foldLeft(true) { case (acc, (value, expected)) =>
          acc && (DotSpace.parse(value) == expected)
        }

        assert(actual)(isTrue)
      },
      test("invalid space value cannot be parsed") {
        val value  = "?"
        val actual = DotSpace.parse(value)
        assert(actual)(isLeft)
      }
    )
}
