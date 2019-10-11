package com.github.gchudnov.parser

import org.apache.kafka.streams.TopologyDescription.Processor
import org.apache.kafka.streams.TopologyDescription.Sink
import org.apache.kafka.streams.TopologyDescription.Source
import org.scalatest.{WordSpec, Matchers, EitherValues}
import scala.io.{Source => IOSource}
import scala.jdk.CollectionConverters._

/**
  * ParserSpec
  *
  * example:
  *   bloop test lib --only com.github.gchudnov.parser.ParserSpec
  */
class ParserSpec extends WordSpec with Matchers with EitherValues {

  "Parser" when {
    "parse fan-out topology description" should {
      "return the parsed structure" in {
        val input = stringFromResource("topologies/fan-out.log")
        val errOrTopology = Parser.run(input)

        errOrTopology.isRight shouldBe true
        errOrTopology.foreach(t => {
          val subtopologies = t.subtopologies()
          subtopologies.size() shouldBe 1

          val subtopology = subtopologies.stream().findFirst().get()
          subtopology.id() shouldBe 0

          val nodes = subtopology.nodes().asScala
          nodes.size shouldBe 5

          nodes.toList.map(_.name()) should contain allElementsOf [String](List(
            "KSTREAM-SINK-0000000003",
            "KSTREAM-MAPVALUES-0000000002",
            "KSTREAM-MAPVALUES-0000000001",
            "KSTREAM-SOURCE-0000000000",
            "KSTREAM-SINK-0000000004"
          ))

          val source = nodes.find(_.name() == "KSTREAM-SOURCE-0000000000").get
          source.predecessors().asScala.isEmpty shouldBe true
          source.successors().asScala.size shouldBe 2

          val proc1 = nodes.find(_.name() == "KSTREAM-MAPVALUES-0000000001").get
          proc1.predecessors().asScala.size shouldBe 1
          proc1.successors().asScala.size shouldBe 1

          val proc2 = nodes.find(_.name() == "KSTREAM-MAPVALUES-0000000002").get
          proc2.predecessors().asScala.size shouldBe 1
          proc2.successors().asScala.size shouldBe 1

          val sink1 = nodes.find(_.name() == "KSTREAM-SINK-0000000003").get
          sink1.predecessors().asScala.size shouldBe 1
          sink1.successors().asScala.isEmpty shouldBe true

          val sink2 = nodes.find(_.name() == "KSTREAM-SINK-0000000004").get
          sink2.predecessors().asScala.size shouldBe 1
          sink2.successors().asScala.isEmpty shouldBe true
        })
      }
    }

    "parse global-store topology description" should {
      "return the valid structure" in {
        val input = stringFromResource("topologies/global-store.log")
        val errOrTopology = Parser.run(input)

        errOrTopology.isRight shouldBe true
        errOrTopology.foreach(t => {
          val subtopologies = t.subtopologies()
          subtopologies.size() shouldBe 1

          val subtopology = subtopologies.stream().findFirst().get()
          subtopology.id() shouldBe 0

          val nodes = subtopology.nodes().asScala
          nodes.size shouldBe 2

          val source = nodes.find(_.name() == "test-source").get.asInstanceOf[Source]
          source.predecessors().asScala.isEmpty shouldBe true
          source.successors().asScala.size shouldBe 1
          source.topicSet().asScala.size shouldBe 1

          val proc = nodes.find(_.name() == "test-processor").get.asInstanceOf[Processor]
          proc.predecessors().asScala.size shouldBe 1
          proc.successors().asScala.size shouldBe 0
          proc.stores().asScala.size shouldBe 1

          val sink = nodes.find(_.isInstanceOf[Sink])
          sink shouldBe None
        })
      }
    }

    "parse complex topology description" should {
      "return the valid structure" in {
        val input = stringFromResource("topologies/complex-topo.log")
        val errOrTopology = Parser.run(input)

        errOrTopology.isRight shouldBe true
        errOrTopology.foreach(t => {
          val subtopologies = t.subtopologies()
          subtopologies.size() shouldBe 2
        })
      }
    }


    "parse an invalid input" should {
      "return an error" in {
        val input = stringFromResource("topologies/invalid-structure.log")
        val errOrTopology = Parser.run(input)

        errOrTopology.left.value.isInstanceOf[ParseException] shouldBe true
      }
    }
  }

  private def stringFromResource(resourcePath: String) = {
    IOSource.fromResource(resourcePath).getLines.mkString("\n")
  }

}
