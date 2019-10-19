package com.github.gchudnov.kprojekt.render

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
      "do not embed the store if 2 processors are connected to it" in {
        /**
          *    p1
          *    /
          *  s1 - p2
          */
        val edges = Seq("p1" -> "s1", "p2" -> "s1")
        val expectedStores = Set.empty[String]
        val actualStores = DotRender.findStoresToEmbed(edges)

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
        val edges = Seq("p1" -> "s1", "p1" -> "s2", "p2" -> "s1", "p3" -> "s3")
        val expectedStores = Set("s3", "s2")
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
  }
}
