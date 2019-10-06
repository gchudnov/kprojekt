package com.github.gchudnov.presenter.render

import org.scalatest.{WordSpec, Matchers}

/**
  * DotRenderSpec
  *
  * example:
  *   bloop test lib --only com.github.gchudnov.presenter.render.DotRenderSpec
  */
class DotRenderSpec extends WordSpec with Matchers {

  "DotRender" when {
    "searching stores to embed" should {
      "return only the stores for subgraphs of size 1" in {

        /**
          *    s3    s2
          *    /      \
          *  p3       p1
          *           /
          *         s1 - p2
          */
        val edges = Seq("p1" -> "s1", "p1" -> "s2", "p2" -> "s1", "p3" -> "s3")
        val expectedStores = Set("s3")

        val actualStores = DotRender.findStoresToEmbed(edges)

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
        val edges = Seq("p1" -> "s1", "p1" -> "s2", "p2" -> "s1", "p2" -> "s2")
        val expectedStores = Set.empty[String]

        val actualStores = DotRender.findStoresToEmbed(edges)

        actualStores shouldBe expectedStores
      }
    }

    "running depth algorithm" should {
      "return the expected values for nodes of an acyclic graph" in {
        val g = Map(
          "s2" -> List("p1"),
          "p1" -> List("s2", "s1"),
          "s1" -> List("p1", "p2"),
          "p2" -> List("s1")
        )

        val finder = DotRender.depth(g)(_)

        val ds2 = finder("s2")
        ds2 shouldBe 3

        val dp1 = finder("p1")
        dp1 shouldBe 2

        val ds1 = finder("s1")
        ds1 shouldBe 2

        val dp2 = finder("p2")
        dp2 shouldBe 3
      }

      "return the expected values for nodes of a cyclic graph" in {
        val g = Map(
          "p1" -> List("s1", "s2"),
          "p2" -> List("s1", "s2"),
          "s2" -> List("p1", "p2"),
          "s1" -> List("p1", "p2")
        )

        val finder = DotRender.depth(g)(_)

        val ds2 = finder("s2")
        ds2 shouldBe 3

        val dp1 = finder("p1")
        dp1 shouldBe 3

        val ds1 = finder("s1")
        ds1 shouldBe 3

        val dp2 = finder("p2")
        dp2 shouldBe 3
      }

      "return the expected values for a graph of one node" in {
        val g = Map(
          "p" -> List.empty[String]
        )

        val finder = DotRender.depth(g)(_)

        val dp = finder("p")
        dp shouldBe 0
      }

      "return the expected values for a non-existent node" in {
        val g = Map(
          "p" -> List.empty[String]
        )

        val finder = DotRender.depth(g)(_)

        val dp = finder("s")
        dp shouldBe 0
      }

      "return the expected values for an empty graph" in {
        val g = Map.empty[String, List[String]]

        val finder = DotRender.depth(g)(_)

        val dp = finder("s")
        dp shouldBe 0
      }
    }
  }

}
