package com.github.gchudnov.kprojekt.formatter.dot.legend

import com.github.gchudnov.kprojekt.ids.{NodeId, ProcessorId}
import com.github.gchudnov.kprojekt.naming.{LiveNamer, Namer, NamerConfig, NodeName}
import zio.test.Assertion.equalTo
import zio.test._
import zio.{Has, ZIO, ZLayer}

object LegendSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("Legend")(
      test("if empty returns no nodes for the given name") {
        val program = for {
          namer <- ZIO.service[Namer]
        } yield {
          val input  = Seq.empty[NodeId]
          val legend = Legend(namer, input)

          val actual = legend.entry("some-name")
          assert(actual)(equalTo(None))
        }

        program.provideLayer(defaultEnv)
      },
      test("if non-empty, nodes can be queries by name") {
        val program = for {
          namer <- ZIO.service[Namer]
        } yield {
          val orininalName1 = "KSTREAM-MAPVALUES-0000000002"
          val originalName2 = "KSTREAM-SELECT-KEY-0000000003"

          val nodeId1 = ProcessorId(orininalName1)
          val nodeId2 = ProcessorId(originalName2)

          val nodeName1 = NodeName(id = Some(2), alias = "MAPVALUES", originalName = orininalName1)
          val nodeName2 = NodeName(id = Some(3), alias = "SELECT.KEY", originalName = originalName2)

          val input  = Seq(nodeId1, nodeId2)
          val legend = Legend(namer, input)

          assert(legend.entry(nodeId1.tId))(equalTo(Some(nodeName1))) &&
          assert(legend.entry(nodeId2.tId))(equalTo(Some(nodeName2)))
        }

        program.provideLayer(defaultEnv)
      }
    )

  private val defaultNameConfig = NamerConfig(maxLenWithoutShortening = 12, separator = ".")

  private val defaultEnv: ZLayer[Any, Nothing, Has[Namer]] = withEnv(defaultNameConfig)

  private def withEnv(nameConfig: NamerConfig): ZLayer[Any, Nothing, Has[Namer]] = {
    val nameConfigEnv = ZLayer.succeed(nameConfig)
    val nameEnv       = (nameConfigEnv >>> LiveNamer.layer)

    val env = nameEnv
    env
  }
}
