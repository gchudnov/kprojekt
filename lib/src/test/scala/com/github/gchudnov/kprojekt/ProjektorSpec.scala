package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.formatter.Dot
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.util.FileOps
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.processor.{ Processor, ProcessorContext, ProcessorSupplier }
import org.apache.kafka.streams.state.{ KeyValueStore, StoreBuilder, Stores }
import org.apache.kafka.streams.{ StreamsBuilder, Topology }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._

/**
 * ProjektorSpec
 *
 * example:
 *   bloop test lib --only com.github.gchudnov.kprojekt.ProjektorSpec
 *
 *   cat graph.dot | dot -Tpng > graph.png
 */
class ProjektorSpec extends AnyWordSpec with Matchers {

  "Projektor" when {
    "rendering a fan-out toplogy" should {
      "produce the expected graphviz output" in {
        import com.github.gchudnov.kprojekt.formatter.dot.DotInstances._

        val expected = FileOps.stringFromResource("graphs/fan-out.dot").toOption.get

        val builder = new StreamsBuilder
        val stream1 = builder.stream[String, String]("topic-a")
        val stream2 = stream1.mapValues(_.toUpperCase())
        val stream3 = stream1.mapValues(_.toLowerCase())
        stream2.to("output-1")
        stream3.to("output-2")

        val topology = builder.build()
        val desc     = topology.describe()
        val actual   = Projektor.run[Dot]("fan-out", desc).trim()

        actual shouldBe expected
      }
    }

    "rendering a word-count topology" should {
      "produce the expected graphviz output" in {
        import com.github.gchudnov.kprojekt.formatter.dot.DotInstances._

        val expected = FileOps.stringFromResource("graphs/word-count.dot").toOption.get

        val builder = new StreamsBuilder
        val source  = builder.stream[String, String]("streams-plaintext-input")
        source
          .flatMapValues(value => value.toLowerCase.split("\\W+").toList.asJava)
          .groupBy((key, value) => value)
          .count(Materialized.as[String, java.lang.Long, KeyValueStore[Bytes, Array[Byte]]]("counts-store"))
          .toStream()
          .to("streams-wordcount-output")

        val topology = builder.build()
        val desc     = topology.describe()
        val actual   = Projektor.run[Dot]("word-count", desc).trim()

        actual shouldBe expected
      }
    }

    "rendreing a topology with global store" should {
      "produce the expected graphviz output" in {
        import com.github.gchudnov.kprojekt.formatter.dot.DotInstances._

        val expected = FileOps.stringFromResource("graphs/global-store.dot").toOption.get

        val stateStoreName = "test-store"

        val processor: Processor[String, String] = new Processor[String, String] {
          var keyValueStore: KeyValueStore[String, java.lang.Long] = _

          override def init(context: ProcessorContext): Unit =
            keyValueStore = context.getStateStore(stateStoreName).asInstanceOf[KeyValueStore[String, java.lang.Long]]

          override def process(key: String, value: String): Unit =
            keyValueStore.put(key, value.length.toLong)

          override def close(): Unit = {}
        }

        val stringSerde = new Serdes.StringSerde
        val longSerde   = new Serdes.LongSerde

        val storeSupplier: StoreBuilder[KeyValueStore[String, java.lang.Long]] = Stores
          .keyValueStoreBuilder(Stores.persistentKeyValueStore(stateStoreName), stringSerde, longSerde)
          .withLoggingDisabled()

        val processorSupplier = new ProcessorSupplier[String, String] {
          override def get(): Processor[String, String] = processor
        }

        val topology = new Topology()

        topology.addGlobalStore(storeSupplier, "test-source", stringSerde.deserializer(), longSerde.deserializer(), "test-topic", "test-processor", processorSupplier)

        val desc   = topology.describe()
        val actual = Projektor.run[Dot]("global-store-usage", desc).trim()

        actual shouldBe expected
      }
    }

    "parsing and rendering a complex topology" should {
      "produce the expected graphviz output" in {
        import com.github.gchudnov.kprojekt.formatter.dot.DotInstances._

        val input         = FileOps.stringFromResource("topologies/complex-topo.log").toOption.get
        val errOrTopology = Parser.run(input)

        errOrTopology.isRight shouldBe true
        errOrTopology.foreach { desc =>
          val actual   = Projektor.run[Dot]("complex-topo", desc).trim()
          val expected = FileOps.stringFromResource("graphs/complex-topo.dot").toOption.get

          actual shouldBe expected
        }
      }
    }
  }
}
