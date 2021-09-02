package com.github.gchudnov.kprojekt.formatter.dot

import com.github.gchudnov.kprojekt.ids.{ NodeId, ProcessorId, SinkId, SourceId, StoreId, TopicId }
import zio.test.Assertion._
import zio.test._

/**
 * DotFormatSpec
 *
 * {{{
 * example:
 *   bloop test lib --only com.github.gchudnov.kprojekt.format.DotFormatSpec
 * }}}
 */
object DotFolderSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("DotFolderSpec")(
      test("node ids can be converted to the expected identifiers") {
        val input    = List[NodeId](TopicId("a"), SourceId("b"), ProcessorId("c"), SinkId("d"), StoreId("e"))
        val expected = List("t_a", "s_b", "p_c", "k_d", "r_e")

        val actual = input.map(DotFolder.toDotId)
        assert(actual)(equalTo(expected))
      },
      test("when name is converted to id, prohibited characters should be removed") {
        val name     = "some-name.txt"
        val expected = "t_some_name_txt"
        val actual   = DotFolder.toDotId(TopicId(name))

        assert(actual)(equalTo(expected))
      },
      test("searching stores to embed should not embed the store if 2 processors are connected to it") {

        /**
         * p1 / s1 - p2
         */
        val edges          = Seq[(NodeId, NodeId)](ProcessorId("p1") -> StoreId("s1"), ProcessorId("p2") -> StoreId("s1"))
        val expectedStores = Set.empty[NodeId]
        val actualStores   = DotFolder.findStoresToEmbed(edges)

        assert(actualStores)(equalTo(expectedStores))
      },
      test("searching stores to embed should return only the stores if there is one connection to it") {

        /**
         * s3 s2 / \ p3 p1 / s1 - p2
         */
        val edges =
          Seq[(NodeId, NodeId)](ProcessorId("p1") -> StoreId("s1"), ProcessorId("p1") -> StoreId("s2"), ProcessorId("p2") -> StoreId("s1"), ProcessorId("p3") -> StoreId("s3"))
        val expectedStores = Set[NodeId](StoreId("s3"), StoreId("s2"))
        val actualStores   = DotFolder.findStoresToEmbed(edges)

        assert(actualStores)(equalTo(expectedStores))
      },
      test("searching stores to embed should return an empty set if no stores match") {

        /**
         * s2 / \ p1 p2 \ / s1
         */
        val edges          = Seq(ProcessorId("p1") -> StoreId("s1"), ProcessorId("p1") -> StoreId("s2"), ProcessorId("p2") -> StoreId("s1"), ProcessorId("p2") -> StoreId("s2"))
        val expectedStores = Set.empty[NodeId]
        val actualStores   = DotFolder.findStoresToEmbed(edges)

        assert(actualStores)(equalTo(expectedStores))
      }
    )
}
