package com.github.gchudnov.presenter

import com.github.gchudnov.presenter.render.Dot
import com.github.gchudnov.presenter.render.DotInstances
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.processor.{Processor, ProcessorContext, ProcessorSupplier}
import org.apache.kafka.streams.state.{KeyValueStore, StoreBuilder, Stores}
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.scalatest.{WordSpec, Matchers}
import scala.jdk.CollectionConverters._

/**
  * PresenterSpec
  *
  * example:
  *   bloop test lib --only com.github.gchudnov.presenter.PresenterSpec
  */
class PresenterSpec extends WordSpec with Matchers {

  "Presenter" when {
    "rendering a fan-out toplogy" should {
      "produce the expected graphviz output" in {
        import DotInstances._

        val builder = new StreamsBuilder
        val stream1 = builder.stream[String, String]("topic-a")
        val stream2 = stream1.mapValues(_.toUpperCase())
        val stream3 = stream1.mapValues(_.toLowerCase())
        stream2.to("output-1")
        stream3.to("output-2")

        val topology = builder.build()
        val desc = topology.describe()
        val str = Presenter.run[Dot]("fan-out", desc)

        str.isEmpty shouldBe false
      }
    }

    "rendering a word-count topology" should {
      "produce the expected graphviz output" in {
        import DotInstances._

        val builder = new StreamsBuilder
        val source = builder.stream[String, String]("streams-plaintext-input")
        source
          .flatMapValues(value => value.toLowerCase.split("\\W+").toList.asJava)
          .groupBy((key, value) => value)
          .count(Materialized.as[String, java.lang.Long, KeyValueStore[Bytes, Array[Byte]]]("counts-store"))
          .toStream()
          .to("streams-wordcount-output")

        val topology = builder.build()
        val desc = topology.describe()
        val str = Presenter.run[Dot]("word-count", desc)

        str.isEmpty shouldBe false
      }
    }

    "rendreing a topology with global store" should {
      "produce the expected graphviz output" in {
        import DotInstances._

        val stateStoreName = "test-store"

        val processor: Processor[String, String] = new Processor[String, String] {
          var keyValueStore: KeyValueStore[String, java.lang.Long] = null

          override def init(context: ProcessorContext): Unit = {
            keyValueStore = context.getStateStore(stateStoreName).asInstanceOf[KeyValueStore[String, java.lang.Long]]
          }

          override def process(key: String, value: String): Unit = {
            keyValueStore.put(key, value.length.toLong)
          }

          override def close(): Unit = {}
        }

        val stringSerde = new Serdes.StringSerde
        val longSerde = new Serdes.LongSerde

        val storeSupplier: StoreBuilder[KeyValueStore[String, java.lang.Long]] = Stores
          .keyValueStoreBuilder(Stores.persistentKeyValueStore(stateStoreName), stringSerde, longSerde)
          .withLoggingDisabled()

        val processorSupplier = new ProcessorSupplier[String, String] {
          override def get(): Processor[String, String] = processor
        }

        val topology = new Topology()

        topology.addGlobalStore(storeSupplier, "test-source", stringSerde.deserializer(), longSerde.deserializer(), "test-topic", "test-processor", processorSupplier)

        val desc = topology.describe()
        val str = Presenter.run[Dot]("global-store-usage", desc)

        import com.github.gchudnov.files.FileOps
        import java.io.File
        FileOps.save(new File("/home/gchudnov/Downloads/graph3.dot"))(str)

        str.isEmpty shouldBe false
      }
    }
  }
}
