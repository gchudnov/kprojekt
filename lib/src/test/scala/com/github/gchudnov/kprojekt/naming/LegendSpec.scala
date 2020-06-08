package com.github.gchudnov.kprojekt.naming

import zio.test.Assertion._
import zio.test._

object LegendSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("Legend")(
      test("if empty returns no nodes for the given name") {
        val input  = Seq.empty[NodeName]
        val legend = Legend(input)

        val actual = legend.nodeName("some-name")
        assert(actual)(equalTo(None))
      },
      test("if non-empty, nodes can be queries by name") {
        val name1 = "KSTREAM-MAPVALUES-0000000002"
        val name2 = "KSTREAM-SELECT-KEY-0000000002"

        val nodeName1 = NodeName(id = Some(2), alias = "1", original = "KSTREAM-MAPVALUES-0000000002")
        val nodeName2 = NodeName(id = Some(3), alias = "2", original = "KSTREAM-SELECT-KEY-0000000003")

        val input  = Seq(nodeName1, nodeName2)
        val legend = Legend(input)

        assert(legend.nodeName(name1))(equalTo(Some(nodeName1))) &&
        assert(legend.nodeName(name2))(equalTo(Some(nodeName2)))
      }
    )
}
