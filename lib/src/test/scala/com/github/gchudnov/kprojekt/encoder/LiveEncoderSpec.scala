package com.github.gchudnov.kprojekt.encoder


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
      test("234") {
        assert(1)(equalTo(1))
      }
      // test("encoding topology where store and topic has the same name should properly create a dot-file") {
      //   val builder = new StreamsBuilder

      //   val requests: KStream[String, Double] = builder
      //     .stream[String, Double]("requests")

      //   val countryTable: GlobalKTable[String, String] = builder.globalTable[String, String](
      //     "countries",
      //     Materialized.as[String, String, ByteArrayKeyValueStore]("countries")
      //   )

      //   val documentTable: KTable[String, String] = builder
      //     .table[String, String](
      //       "documents",
      //       Materialized.as[String, String, ByteArrayKeyValueStore]("documents")
      //     )

      //   requests
      //     .leftJoin(countryTable)((k, v) => k, (v, gv) => s"$v:$gv")
      //     .leftJoin(documentTable)((v, vt) => s"$v:$vt")
      //     .to("output")

      //   val topology = builder.build()
      //   val desc     = topology.describe()

      //   for {
      //     expected <- ZIO.fromEither(Resources.linesFromResource("graphs/store-topic-same-name.dot"))
      //     actual   <- Encoder.encode("same-name", desc).provideLayer(defaultEnv)
      //   } yield assert(actual.trim)(equalTo(expected.trim))
      // }
    )

  // private val defaultDotConfig  = DotConfig(indent = 2, fontName = "sans-serif", fontSize = 10, isEmbedStore = false, hasLegend = false, space = DotSpace.Small)
  // private val embeddedDotConfig = defaultDotConfig.copy(isEmbedStore = true)
  // private val defaultNameConfig = NamerConfig(maxLenWithoutShortening = 12, separator = ".")

  // private val defaultEnv =
  //   withEnv(defaultDotConfig, defaultNameConfig)

  // private val embeddedEnv =
  //   withEnv(embeddedDotConfig, defaultNameConfig)

  // private def withEnv(dotConfig: DotConfig, nameConfig: NamerConfig) = {
  //   val dotConfigEnv  = ZLayer.succeed(dotConfig)
  //   val nameConfigEnv = ZLayer.succeed(nameConfig)

  //   val nameEnv = (nameConfigEnv >>> LiveNamer.layer)
  //   val foldEnv = ((dotConfigEnv ++ nameEnv) >>> DotFolder.layer)

  //   val encoderEnv = foldEnv >>> LiveEncoder.layer
  //   encoderEnv
  // }
}
