package com.github.gchudnov.kprojekt.formatter.dot

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * DotFormatSpec
 *
 * example:
 *   bloop test lib --only com.github.gchudnov.kprojekt.format.DotFormatSpec
 */
class DotFormatSpec extends AnyWordSpec with Matchers {

  "DotFormat" when {

    "name is converted to id" should {
      "remove the prohibited characters" in {
        val name = "some-name.txt"

        val expected = "some_name_txt"
        val actual   = DotFormat.toId(name)

        actual shouldBe expected
      }
    }

    "searching stores to embed" should {
      "do not embed the store if 2 processors are connected to it" in {

        /**
         *    p1
         *    /
         *  s1 - p2
         */
        val edges          = Seq("p1" -> "s1", "p2" -> "s1")
        val expectedStores = Set.empty[String]
        val actualStores   = DotFormat.findStoresToEmbed(edges)

        actualStores shouldBe expectedStores
      }

      "return only the stores if there is one connection to it" in {

        /**
         *    s3    s2
         *    /      \
         *  p3       p1
         *           /
         *         s1 - p2
         */
        val edges          = Seq("p1" -> "s1", "p1" -> "s2", "p2" -> "s1", "p3" -> "s3")
        val expectedStores = Set("s3", "s2")
        val actualStores   = DotFormat.findStoresToEmbed(edges)

        actualStores shouldBe expectedStores
      }

      "return an empty set if no stores match" in {

        /**
         *   s2
         *  /  \
         * p1   p2
         *  \  /
         *   s1
         */
        val edges          = Seq("p1" -> "s1", "p1" -> "s2", "p2" -> "s1", "p2" -> "s2")
        val expectedStores = Set.empty[String]
        val actualStores   = DotFormat.findStoresToEmbed(edges)

        actualStores shouldBe expectedStores
      }
    }
  }
}
