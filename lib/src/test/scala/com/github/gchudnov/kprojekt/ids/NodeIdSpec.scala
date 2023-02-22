package com.github.gchudnov.kprojekt.ids

import zio.test.Assertion.equalTo
import zio.test._
// import NodeIdOrdering._

object NodeIdSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("NodeId")(
      test("234") {
        assert(1)(equalTo(1))
      }
      // test("can be ordered") {
      //   val topicId  = TopicId("a")     // t:a
      //   val sourceId = SourceId("b")    // s:b
      //   val procId   = ProcessorId("c") // p:c
      //   val sinkId   = SinkId("d")      // k:d
      //   val storeId  = StoreId("e")     // r:e

      //   val ns       = List[NodeId](procId, storeId, sourceId, topicId, sinkId)
      //   val actual   = ns.sorted
      //   val expected = List[NodeId](topicId, sourceId, procId, sinkId, storeId)

      //   assert(actual)(equalTo(expected))
      // },
      // test("distinct elements can be filtered") {
      //   val topicId1 = TopicId("a")     // t:a
      //   val topicId2 = TopicId("a")     // t:a
      //   val sourceId = SourceId("b")    // s:b
      //   val procId   = ProcessorId("c") // p:c
      //   val sinkId   = SinkId("d")      // k:d
      //   val storeId  = StoreId("e")     // r:e

      //   val ns       = List[NodeId](topicId1, sourceId, procId, sinkId, storeId, topicId2)
      //   val actual   = ns.distinct.sorted
      //   val expected = List[NodeId](topicId1, sourceId, procId, sinkId, storeId)

      //   assert(actual)(equalTo(expected))
      // }
    )
}
