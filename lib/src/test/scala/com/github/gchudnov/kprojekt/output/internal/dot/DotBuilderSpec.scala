package com.github.gchudnov.kprojekt.output.internal.dot

import com.github.gchudnov.kprojekt.output.Id
import com.github.gchudnov.kprojekt.output.internal.dot.DotBuilder
import zio.test.Assertion._
import zio.test._

object DotBuilderSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("DotBuilder")(
      test("node ids can be converted to the identifiers") {
        val input    = List(Id.topic("a"), Id.source("b"), Id.processor("c"), Id.sink("d"), Id.store("e"))
        val expected = List("t_a", "s_b", "p_c", "k_d", "r_e")

        val actual = input.map(DotBuilder.toDotId)

        assert(actual)(equalTo(expected))
      },
      test("when name is converted to id, prohibited characters should be removed") {
        val name     = "some-name.txt"
        val expected = "t_some_name_txt"

        val actual   = DotBuilder.toDotId(Id.topic(name))

        assert(actual)(equalTo(expected))
      },
    )
}
