package com.github.gchudnov.kprojekt.legend

import zio.test.Assertion._
import zio.test._

object LegendSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("LegendSpec")(
      test("name alias can be calculated") {
        val input = "KSTREAM-MAPVALUES-0000000002"
        val actual = input
        val expected = Legend.alias(input)

        assert(actual)(equalTo(expected))
      },
      test("name aliases can be created") {
        val input = Seq("KSTREAM-MAPVALUES-0000000002", "KSTREAM-SELECT-KEY-0000000002")
        val actual = Map(
          "KSTREAM-MAPVALUES-0000000002" -> "KSTREAM-MAPVALUES-0000000002",
          "KSTREAM-SELECT-KEY-0000000002" -> "KSTREAM-SELECT-KEY-0000000002"
        )
        val expected = Legend.build(input)

        assert(actual)(equalTo(expected))
      }
    )
}
