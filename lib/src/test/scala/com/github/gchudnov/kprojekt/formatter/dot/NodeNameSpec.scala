package com.github.gchudnov.kprojekt.formatter.dot

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * NodeNameSpec
 *
 * example:
 *   bloop test lib --only com.github.gchudnov.name.NodeNameSpec
 */
class NodeNameSpec extends AnyWordSpec with Matchers with EitherValues {

  "NodeName" when {
    "stream name with one-word name is parsed" should {
      "correctly split it in parts" in {
        val name     = "KSTREAM-MAPVALUES-0000000002"
        val expected = DotNodeName("KSTREAM", "MAPVALUES", "0000000002")

        val actual = DotNodeName.parse(name)
        actual shouldBe expected
      }
    }

    "stream name with two-word name is parsed" should {
      "correctly split it in parts" in {
        val name     = "KSTREAM-SELECT-KEY-0000000002"
        val expected = DotNodeName("KSTREAM", "SELECT-KEY", "0000000002")

        val actual = DotNodeName.parse(name)
        actual shouldBe expected
      }
    }

    "stream with custom name is parsed" should {
      "return the original name" in {
        val name     = "some-custom-name"
        val expected = DotNodeName("", name, "")

        val actual = DotNodeName.parse(name)
        actual shouldBe expected
      }
    }
  }

}
