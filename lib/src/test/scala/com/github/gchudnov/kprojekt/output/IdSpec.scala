package com.github.gchudnov.kprojekt.output

import zio.test.Assertion._
import zio.test._
import Id._

object NodeIdSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("Id")(
      test("ordering") {
        val topicId  = Id.topic("a")     // t:a
        val sourceId = Id.source("b")    // s:b
        val procId   = Id.processor("c") // p:c
        val sinkId   = Id.sink("d")      // k:d
        val storeId  = Id.store("e")     // r:e

        val ns       = List(procId, storeId, sourceId, topicId, sinkId)
        val actual   = ns.sorted
        val expected = List(sinkId, procId, storeId, sourceId, topicId)

        assert(actual)(equalTo(expected))
      },
      test("distinct elements can be filtered-out") {
        val topicId1 = Id.topic("a")     // t:a
        val topicId2 = Id.topic("a")     // t:a
        val sourceId = Id.source("b")    // s:b
        val procId   = Id.processor("c") // p:c
        val sinkId   = Id.sink("d")      // k:d
        val storeId  = Id.store("e")     // r:e

        val ns       = List(topicId1, sourceId, procId, sinkId, storeId, topicId2)
        val actual   = ns.distinct.sorted
        val expected = List(sinkId, procId, storeId, sourceId, topicId1)

        assert(actual)(equalTo(expected))
      }
    )
}
