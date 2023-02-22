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
import org.apache.kafka.streams.scala._
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
      test("DOT - encode a fan-out topology") {
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
      test("DOT - encode the word-count topology") {
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
    )

  private def makeEnv: ULayer[Writer] = {
    val dotEnv = Builder.dot
    val writerEnv = dotEnv >>> Writer.make

    val env = writerEnv

    env
  }
}

          // _ = Files.saveString(Paths.get("/home/gchudnov/Projects/kprojekt/lib/src/test/resources/graphs/fan-out.dot").toFile, actual)
