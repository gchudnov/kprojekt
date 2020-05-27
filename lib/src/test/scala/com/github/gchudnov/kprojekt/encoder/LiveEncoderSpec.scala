package com.github.gchudnov.kprojekt.encoder

import com.github.gchudnov.kprojekt.formatter.{ Folder }
import com.github.gchudnov.kprojekt.formatter.dot.DotConfig
import com.github.gchudnov.kprojekt.util.FileOps
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.{ StreamsBuilder, Topology }
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.processor.{ Processor, ProcessorContext, ProcessorSupplier }
import org.apache.kafka.streams.state.{ KeyValueStore, StoreBuilder, Stores }
import zio.{ ZIO, ZLayer }
import zio.test.Assertion._
import zio.test._

import scala.jdk.CollectionConverters._

object LiveEncoderSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("EncoderSpec")(
      test("resource should be non-empty") {
        val errOrData = FileOps.stringFromResource("graphs/fan-out.dot")
        assert(errOrData)(isRight) &&
        assert(errOrData.toTry.get)(isNonEmptyString)
      },
      testM("encoding a fan-out topology should produce the expected graphviz output") {
        val builder = new StreamsBuilder
        val stream1 = builder.stream[String, String]("topic-a")
        val stream2 = stream1.mapValues(_.toUpperCase())
        val stream3 = stream1.mapValues(_.toLowerCase())
        stream2.to("output-1")
        stream3.to("output-2")

        val topology = builder.build()
        val desc     = topology.describe()

        for {
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/fan-out.dot"))
          actual   <- Encoder.encode("fan-out", desc).provideLayer(Encoder.live).provideLayer(Folder.live).provideLayer(withConfig(defaultConfig))
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      testM("encoding the word-count topology should produce the expected graphviz output") {
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

        for {
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/word-count.dot"))
          actual   <- Encoder.encode("word-count", desc).provideLayer(Encoder.live).provideLayer(Folder.live).provideLayer(withConfig(defaultConfig))
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      testM("encoding the word-count topology should produce the expected graphviz output (embed stores)") {
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

        for {
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/word-count-embed.dot"))
          actual   <- Encoder.encode("word-count", desc).provideLayer(Encoder.live).provideLayer(Folder.live).provideLayer(withConfig(embedConfig))
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      testM("encoding a topology with global store should produce the expected graphviz output") {
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
        val desc = topology.describe()

        for {
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/global-store.dot"))
          actual   <- Encoder.encode("global-store-usage", desc).provideLayer(Encoder.live).provideLayer(Folder.live).provideLayer(withConfig(defaultConfig))
        } yield assert(actual.trim)(equalTo(expected.trim))
      }
    )

  def withConfig(c: DotConfig): ZLayer[Any, Nothing, DotConfig] =
    ZLayer.succeedMany(c)

  private val defaultConfig = DotConfig(indent = 2, fontName = "sans-serif", fontSize = 10, isEmbedStore = false)
  private val embedConfig   = defaultConfig.copy(isEmbedStore = true)
}
