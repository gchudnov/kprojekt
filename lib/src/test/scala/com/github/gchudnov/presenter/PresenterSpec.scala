package com.github.gchudnov.presenter

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.StreamsBuilder
import org.scalatest.{WordSpec, Matchers}
import scala.jdk.CollectionConverters._
import com.github.gchudnov.presenter.render.Dot
import com.github.gchudnov.presenter.render.DotInstances

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

        println(desc)
        println(str)
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

        println(desc)
        println(str)
      }
    }
  }

  private def saveAsFile(filename: String, data: String) = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(data)
    bw.close()
  }
}
