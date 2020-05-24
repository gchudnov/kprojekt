package com.github.gchudnov.kprojekt.formatter.dot

import zio.test.Assertion._
import zio.test._

/**
 * DotFormatSpec
 *
 * example:
 *   bloop test lib --only com.github.gchudnov.kprojekt.format.DotFormatSpec
 */
object DotFolderSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("DotFolderSpec")(
      test("when name is converted to id, prohibited characters should be removed") {
        val name     = "some-name.txt"
        val expected = "some_name_txt"
        val actual   = DotFolder.toId(name)

        assert(actual)(equalTo(expected))
      },
      test("searching stores to embed should not embed the store if 2 processors are connected to it") {

        /**
         *    p1
         *    /
         *  s1 - p2
         */
        val edges          = Seq("p1" -> "s1", "p2" -> "s1")
        val expectedStores = Set.empty[String]
        val actualStores   = DotFolder.findStoresToEmbed(edges)

        assert(actualStores)(equalTo(expectedStores))
      },
      test("searching stores to embed should return only the stores if there is one connection to it") {

        /**
         *    s3    s2
         *    /      \
         *  p3       p1
         *           /
         *         s1 - p2
         */
        val edges          = Seq("p1" -> "s1", "p1" -> "s2", "p2" -> "s1", "p3" -> "s3")
        val expectedStores = Set("s3", "s2")
        val actualStores   = DotFolder.findStoresToEmbed(edges)

        assert(actualStores)(equalTo(expectedStores))
      },
      test("searching stores to embed should return an empty set if no stores match") {

        /**
         *   s2
         *  /  \
         * p1   p2
         *  \  /
         *   s1
         */
        val edges          = Seq("p1" -> "s1", "p1" -> "s2", "p2" -> "s1", "p2" -> "s2")
        val expectedStores = Set.empty[String]
        val actualStores   = DotFolder.findStoresToEmbed(edges)

        assert(actualStores)(equalTo(expectedStores))
      }
    )
}
