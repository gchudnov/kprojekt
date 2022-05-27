package com.github.gchudnov.kprojekt.encoder

import com.github.gchudnov.kprojekt.formatter.dot.{ DotConfig, DotFolder, DotSpace }
import com.github.gchudnov.kprojekt.naming.{ LiveNamer, NamerConfig }
import com.github.gchudnov.kprojekt.util.FileOps
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.processor.api.{ Processor, ProcessorContext, ProcessorSupplier, Record }
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.kstream.{ KStream, KTable, Materialized }
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.scala.{ ByteArrayKeyValueStore, StreamsBuilder }
import org.apache.kafka.streams.state.{ KeyValueStore, StoreBuilder, Stores }
import zio.test.Assertion._
import zio.test._
import zio._

object LiveEncoderSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("EncoderSpec")(
      test("resource should be non-empty") {
        val errOrData = FileOps.stringFromResource("graphs/fan-out.dot")
        assert(errOrData)(isRight) &&
        assert(errOrData.toTry.get)(isNonEmptyString)
      },
      test("encoding a fan-out topology should produce the expected graphviz output") {
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
          actual   <- Encoder.encode("fan-out", desc).provideLayer(defaultEnv)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      test("encoding the word-count topology should produce the expected graphviz output") {
        val builder = new StreamsBuilder
        val source  = builder.stream[String, String]("streams-plaintext-input")

        source
          .flatMapValues(_.toLowerCase.split("\\W+").toList)
          .groupBy((key, value) => value)
          .count()(Materialized.as[String, Long, ByteArrayKeyValueStore]("counts-store"))
          .toStream
          .to("streams-wordcount-output")

        val topology = builder.build()
        val desc     = topology.describe()

        for {
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/word-count.dot"))
          actual   <- Encoder.encode("word-count", desc).provideLayer(defaultEnv)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      test("encoding the word-count topology should produce the expected graphviz output (embed stores)") {
        val builder = new StreamsBuilder
        val source  = builder.stream[String, String]("streams-plaintext-input")
        source
          .flatMapValues(value => value.toLowerCase.split("\\W+").toList)
          .groupBy((key, value) => value)
          .count()(Materialized.as[String, Long, ByteArrayKeyValueStore]("counts-store"))
          .toStream
          .to("streams-wordcount-output")

        val topology = builder.build()
        val desc     = topology.describe()

        for {
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/word-count-embed.dot"))
          actual   <- Encoder.encode("word-count", desc).provideLayer(embeddedEnv)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      test("encoding a topology with global store should produce the expected graphviz output") {
        val stateStoreName = "test-store"

        def processor: Processor[String, Long, Void, Void] = new Processor[String, Long, Void, Void] {
          var keyValueStore: KeyValueStore[String, Long] = _

          override def init(context: ProcessorContext[Void, Void]): Unit =
            keyValueStore = context.getStateStore(stateStoreName).asInstanceOf[KeyValueStore[String, Long]]

          override def process(record: Record[String, Long]): Unit =
            keyValueStore.put(record.key(), record.value())

          override def close(): Unit = {}
        }

        val storeSupplier: StoreBuilder[KeyValueStore[String, Long]] = Stores
          .keyValueStoreBuilder(Stores.persistentKeyValueStore(stateStoreName), stringSerde, longSerde)
          .withLoggingDisabled()

        val processorSupplier: ProcessorSupplier[String, Long, Void, Void] = () => processor

        val topology = new Topology()
        topology.addGlobalStore(storeSupplier, "test-source", stringSerde.deserializer(), longSerde.deserializer(), "test-topic", "test-processor", processorSupplier)
        val desc = topology.describe()

        for {
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/global-store.dot"))
          actual   <- Encoder.encode("global-store-usage", desc).provideLayer(defaultEnv)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      test("encoding topology where store and topic has the same name should properly create a dot-file") {
        val builder = new StreamsBuilder

        val requests: KStream[String, Double] = builder
          .stream[String, Double]("requests")

        val countryTable: GlobalKTable[String, String] = builder.globalTable[String, String](
          "countries",
          Materialized.as[String, String, ByteArrayKeyValueStore]("countries")
        )

        val documentTable: KTable[String, String] = builder
          .table[String, String](
            "documents",
            Materialized.as[String, String, ByteArrayKeyValueStore]("documents")
          )

        requests
          .leftJoin(countryTable)((k, v) => k, (v, gv) => s"$v:$gv")
          .leftJoin(documentTable)((v, vt) => s"$v:$vt")
          .to("output")

        val topology = builder.build()
        val desc     = topology.describe()

        for {
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/store-topic-same-name.dot"))
          actual   <- Encoder.encode("same-name", desc).provideLayer(defaultEnv)
        } yield assert(actual.trim)(equalTo(expected.trim))
      }
    )

  private val defaultDotConfig  = DotConfig(indent = 2, fontName = "sans-serif", fontSize = 10, isEmbedStore = false, hasLegend = false, space = DotSpace.Small)
  private val embeddedDotConfig = defaultDotConfig.copy(isEmbedStore = true)
  private val defaultNameConfig = NamerConfig(maxLenWithoutShortening = 12, separator = ".")

  private val defaultEnv =
    withEnv(defaultDotConfig, defaultNameConfig)

  private val embeddedEnv =
    withEnv(embeddedDotConfig, defaultNameConfig)

  private def withEnv(dotConfig: DotConfig, nameConfig: NamerConfig) = {
    val dotConfigEnv  = ZLayer.succeed(dotConfig)
    val nameConfigEnv = ZLayer.succeed(nameConfig)

    val nameEnv = (nameConfigEnv >>> LiveNamer.layer)
    val foldEnv = ((dotConfigEnv ++ nameEnv) >>> DotFolder.layer)

    val encoderEnv = foldEnv >>> LiveEncoder.layer
    encoderEnv
  }
}
