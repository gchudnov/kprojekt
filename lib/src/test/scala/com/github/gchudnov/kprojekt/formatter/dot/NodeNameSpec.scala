package com.github.gchudnov.kprojekt.formatter.dot

import zio.test.Assertion._
import zio.test._

/**
 * NodeNameSpec
 *
 * example:
 *   bloop test lib --only com.github.gchudnov.name.NodeNameSpec
 */
object NodeNameSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("NodeNameSpec")(
      test("stream name with one-word name is parsed should correctly split it in parts") {
        val name     = "KSTREAM-MAPVALUES-0000000002"
        val expected = DotNodeName("KSTREAM", "MAPVALUES", "0000000002")

        val actual = DotNodeName.parse(name)
        assert(actual)(equalTo(expected))
      },
      test("stream name with two-word name is parsed should correctly split it in parts") {
        val name     = "KSTREAM-SELECT-KEY-0000000002"
        val expected = DotNodeName("KSTREAM", "SELECT-KEY", "0000000002")

        val actual = DotNodeName.parse(name)
        assert(actual)(equalTo(expected))
      },
      test("stream with custom name is parsed should return the original name") {
        val name     = "some-custom-name"
        val expected = DotNodeName("", name, "")

        val actual = DotNodeName.parse(name)
        assert(actual)(equalTo(expected))
      }
    )
}
