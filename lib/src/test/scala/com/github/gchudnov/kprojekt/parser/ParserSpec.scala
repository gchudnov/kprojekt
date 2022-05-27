package com.github.gchudnov.kprojekt.parser

import com.github.gchudnov.kprojekt.util.FileOps
import org.apache.kafka.streams.TopologyDescription.{ Processor, Sink, Source }
import zio.{ Scope, ZIO }
import zio.test.Assertion._
import zio.test._

import scala.jdk.CollectionConverters._

/**
 * ParserSpec
 *
 * {{{
 * example:
 *   bloop test lib --only com.github.gchudnov.parser.ParserSpec
 * }}}
 */
object ParserSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ParserSpec")(
      test("parse fan-out topology description should return the parsed structure") {
        for {
          input <- ZIO.fromEither(FileOps.stringFromResource("topologies/fan-out.log"))
          desc  <- Parser.run(input).provideLayer(defaultEnv)
        } yield {
          val subtopologies = desc.subtopologies().asScala.toSeq
          val subtopology   = subtopologies.head
          val nodes         = subtopology.nodes().asScala
          val source        = nodes.find(_.name() == "KSTREAM-SOURCE-0000000000").get
          val proc1         = nodes.find(_.name() == "KSTREAM-MAPVALUES-0000000001").get
          val proc2         = nodes.find(_.name() == "KSTREAM-MAPVALUES-0000000002").get
          val sink1         = nodes.find(_.name() == "KSTREAM-SINK-0000000003").get
          val sink2         = nodes.find(_.name() == "KSTREAM-SINK-0000000004").get

          assert(subtopologies)(hasSize(equalTo(1))) &&
          assert(subtopology)(hasField("id", _.id(), equalTo(0))) &&
          assert(nodes)(hasSize(equalTo(5))) &&
          assert(nodes.map(_.name()))(
            hasSameElements(
              List(
                "KSTREAM-SINK-0000000003",
                "KSTREAM-MAPVALUES-0000000002",
                "KSTREAM-MAPVALUES-0000000001",
                "KSTREAM-SOURCE-0000000000",
                "KSTREAM-SINK-0000000004"
              )
            )
          ) &&
          assert(
            source
              .asInstanceOf[Source]
              .topicSet()
              .asScala
              .toSet
          )(equalTo(Set("A", "A.B", "A-B", "A_B"))) &&
          assert(source.predecessors().asScala)(isEmpty) &&
          assert(source.successors().asScala)(hasSize(equalTo(2))) &&
          assert(proc1.predecessors().asScala)(hasSize(equalTo(1))) &&
          assert(proc1.successors().asScala)(hasSize(equalTo(1))) &&
          assert(proc2.predecessors().asScala)(hasSize(equalTo(1))) &&
          assert(proc2.successors().asScala)(hasSize(equalTo(1))) &&
          assert(sink1.predecessors().asScala)(hasSize(equalTo(1))) &&
          assert(sink1.successors().asScala)(hasSize(equalTo(0))) &&
          assert(sink2.predecessors().asScala)(hasSize(equalTo(1))) &&
          assert(sink2.successors().asScala)(hasSize(equalTo(0)))
        }
      },
      test("parse global-store topology description should return the valid structure") {
        for {
          input <- ZIO.fromEither(FileOps.stringFromResource("topologies/global-store.log"))
          desc  <- Parser.run(input).provideLayer(defaultEnv)
        } yield {
          val subtopologies = desc.subtopologies().asScala.toSeq
          val subtopology   = subtopologies.head
          val nodes         = subtopology.nodes().asScala
          val source        = nodes.find(_.name() == "test-source").get.asInstanceOf[Source]
          val proc          = nodes.find(_.name() == "test-processor").get.asInstanceOf[Processor]
          val sink          = nodes.find(_.isInstanceOf[Sink])

          assert(subtopologies)(hasSize(equalTo(1))) &&
          assert(subtopology)(hasField("id", _.id(), equalTo(0))) &&
          assert(nodes)(hasSize(equalTo(2))) &&
          assert(source.predecessors().asScala)(hasSize(equalTo(0))) &&
          assert(source.successors().asScala)(hasSize(equalTo(1))) &&
          assert(source.topicSet().asScala)(hasSize(equalTo(1))) &&
          assert(proc.predecessors().asScala)(hasSize(equalTo(1))) &&
          assert(proc.successors().asScala)(hasSize(equalTo(0))) &&
          assert(proc.stores().asScala)(hasSize(equalTo(1))) &&
          assert(sink)(isNone)
        }
      },
      test("parse complex topology description should return the valid structure") {
        for {
          input <- ZIO.fromEither(FileOps.stringFromResource("topologies/complex-topo-1.log"))
          desc  <- Parser.run(input).provideLayer(defaultEnv)
        } yield {
          val subtopologies = desc.subtopologies().asScala.toSet
          val subtopology0  = subtopologies.find(_.id() == 0).get
          val subtopology1  = subtopologies.find(_.id() == 1).get
          val nodes0        = subtopology0.nodes().asScala
          val nodes1        = subtopology1.nodes().asScala

          assert(subtopologies)(hasSize(equalTo(2))) &&
          assert(subtopology0)(hasField("id", _.id(), equalTo(0))) &&
          assert(subtopology1)(hasField("id", _.id(), equalTo(1))) &&
          assert(nodes0)(hasSize(equalTo(7))) &&
          assert(nodes1)(hasSize(equalTo(19)))
        }
      },
      test("parse an invalid input should return an error") {
        for {
          input <- ZIO.fromEither(FileOps.stringFromResource("topologies/invalid-structure.log"))
          res   <- Parser.run(input).provideLayer(defaultEnv).exit
        } yield assert(res)(fails(isSubtype[ParseException](anything)))
      }
    )

  private val defaultEnv =
    LiveParser.layer
}
