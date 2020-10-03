package com.github.gchudnov.kprojekt.encoder

import com.github.gchudnov.kprojekt.formatter.Folder
import com.github.gchudnov.kprojekt.formatter.dot.{ DotConfig, DotSpace }
import com.github.gchudnov.kprojekt.naming.{ Namer, NamerConfig }
import com.github.gchudnov.kprojekt.util.FileOps
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.processor.{ Processor, ProcessorContext, ProcessorSupplier }
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes._
import org.apache.kafka.streams.scala.kstream.{ KStream, KTable, Materialized }
import org.apache.kafka.streams.scala.{ ByteArrayKeyValueStore, Serdes, StreamsBuilder }
import org.apache.kafka.streams.state.{ KeyValueStore, StoreBuilder, Stores }
import zio.test.Assertion._
import zio.test._
import zio.{ Has, ZIO, ZLayer }

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
          actual   <- Encoder.encode("fan-out", desc).provideLayer(defaultEnv)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      testM("encoding the word-count topology should produce the expected graphviz output") {
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
      testM("encoding the word-count topology should produce the expected graphviz output (embed stores)") {
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
      testM("encoding a topology with global store should produce the expected graphviz output") {
        val stateStoreName = "test-store"

        val processor: Processor[String, Long] = new Processor[String, Long] {
          var keyValueStore: KeyValueStore[String, Long] = _

          override def init(context: ProcessorContext): Unit =
            keyValueStore = context.getStateStore(stateStoreName).asInstanceOf[KeyValueStore[String, Long]]

          override def process(key: String, value: Long): Unit =
            keyValueStore.put(key, value)

          override def close(): Unit = {}
        }

        val storeSupplier: StoreBuilder[KeyValueStore[String, Long]] = Stores
          .keyValueStoreBuilder(Stores.persistentKeyValueStore(stateStoreName), Serdes.String, Serdes.Long)
          .withLoggingDisabled()

        val processorSupplier = new ProcessorSupplier[String, Long] {
          override def get(): Processor[String, Long] = processor
        }

        val topology = new Topology()
        topology.addGlobalStore(storeSupplier, "test-source", Serdes.String.deserializer(), Serdes.Long.deserializer(), "test-topic", "test-processor", processorSupplier)
        val desc = topology.describe()

        for {
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/global-store.dot"))
          actual   <- Encoder.encode("global-store-usage", desc).provideLayer(defaultEnv)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      testM("encoding topology where store and topic has the same name should properly create a dot-file") {
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

  private val defaultEnv: ZLayer[Any, Nothing, Has[Encoder.Service]] =
    withEnv(defaultDotConfig, defaultNameConfig)

  private val embeddedEnv: ZLayer[Any, Nothing, Has[Encoder.Service]] =
    withEnv(embeddedDotConfig, defaultNameConfig)

  private def withEnv(dotConfig: DotConfig, nameConfig: NamerConfig): ZLayer[Any, Nothing, Has[Encoder.Service]] = {
    val dotConfigEnv  = ZLayer.succeed(dotConfig)
    val nameConfigEnv = ZLayer.succeed(nameConfig)

    val nameEnv = (nameConfigEnv >>> Namer.live)
    val foldEnv = ((dotConfigEnv ++ nameEnv) >>> Folder.live)

    val encoderEnv = foldEnv >>> Encoder.live
    encoderEnv
  }
}
