package com.github.gchudnov.kprojekt.output

import com.github.gchudnov.kprojekt.output.Id
import com.github.gchudnov.kprojekt.output.Builder
import com.github.gchudnov.kprojekt.util.Resources
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.processor.api.{ Processor, ProcessorContext, ProcessorSupplier, Record }
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.kstream.{ KStream, KTable, Materialized }
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.scala.{ ByteArrayKeyValueStore, StreamsBuilder }
import org.apache.kafka.streams.state.{ KeyValueStore, StoreBuilder, Stores }
import zio._
import zio.test.Assertion._
import zio.test._
import com.github.gchudnov.kprojekt.output.Builder

import com.github.gchudnov.kprojekt.util.Files
import java.nio.file.Paths


object WriterSpec extends ZIOSpecDefault {

  private val env = makeEnv

  override def spec: Spec[TestEnvironment, Any] =
    suite("Writer")(
      test("DOT - write the fan-out topology") {
        val builder = new StreamsBuilder

        val stream1 = builder.stream[String, String]("topic-a")
        val stream2 = stream1.mapValues(_.toUpperCase())
        val stream3 = stream1.mapValues(_.toLowerCase())
        stream2.to("output-1")
        stream3.to("output-2")

        val topology = builder.build()
        val desc     = topology.describe()

        for {
          expected <- ZIO.fromEither(Resources.linesFromResource("graphs/fan-out.dot"))
          actual   <- Writer.write("fan-out", desc).provideLayer(env)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      test("DOT - write the word-count topology") {
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
          expected <- ZIO.fromEither(Resources.linesFromResource("graphs/word-count.dot"))
          actual   <- Writer.write("word-count", desc).provideLayer(env)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      test("DOT - write the topology with global store") {
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
          expected <- ZIO.fromEither(Resources.linesFromResource("graphs/global-store.dot"))
          actual   <- Writer.write("global-store-usage", desc).provideLayer(env)
        } yield assert(actual.trim)(equalTo(expected.trim))
      },
      test("DOT - encoding topology where store and topic has the same name") {
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
          expected <- ZIO.fromEither(Resources.linesFromResource("graphs/store-topic-same-name.dot"))
          actual   <- Writer.write("same-name", desc).provideLayer(env)
        } yield assert(actual.trim)(equalTo(expected.trim))
      }
    )

  private def makeEnv: ULayer[Writer] = {
    val dotEnv = Builder.dot
    val writerEnv = dotEnv >>> Writer.make

    val env = writerEnv

    env
  }
}

          // _ = Files.saveString(Paths.get("/home/gchudnov/Projects/kprojekt/lib/src/test/resources/graphs/fan-out.dot").toFile, actual)
