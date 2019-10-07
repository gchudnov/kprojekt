package com.github.gchudnov.name

import org.scalatest.{WordSpec, Matchers, EitherValues}

/**
  * NodeNameSpec
  *
  * example:
  *   bloop test lib --only com.github.gchudnov.name.NodeNameSpec
  */
class NodeNameSpec extends WordSpec with Matchers with EitherValues {

  "NodeName" when {
    "stream name with one-word name is parsed" should {
      "correctly split it in parts" in {
        val name = "KSTREAM-MAPVALUES-0000000002"
        val expected = NodeName("KSTREAM", "MAPVALUES", "0000000002")

        val actual = NodeName.parse(name)
        actual shouldBe expected
      }
    }

    "stream name with two-word name is parsed" should {
      "correctly split it in parts" in {
        val name = "KSTREAM-SELECT-KEY-0000000002"
        val expected = NodeName("KSTREAM", "SELECT-KEY", "0000000002")

        val actual = NodeName.parse(name)
        actual shouldBe expected
      }
    }

    "stream with custom name is parsed" should {
      "return the original name" in {
        val name = "some-custom-name"
        val expected = NodeName("", name, "")

        val actual = NodeName.parse(name)
        actual shouldBe expected
      }
    }
  }

}