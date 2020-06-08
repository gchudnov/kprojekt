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
        val orininal1 = "KSTREAM-MAPVALUES-0000000002"
        val original2 = "KSTREAM-SELECT-KEY-0000000002"

        val nodeName1 = NodeName(id = Some(2), alias = "1", original = orininal1)
        val nodeName2 = NodeName(id = Some(3), alias = "2", original = original2)

        val input  = Seq(nodeName1, nodeName2)
        val legend = Legend(input)

        assert(legend.nodeName(orininal1))(equalTo(Some(nodeName1))) &&
        assert(legend.nodeName(original2))(equalTo(Some(nodeName2)))
      }
    )
}
