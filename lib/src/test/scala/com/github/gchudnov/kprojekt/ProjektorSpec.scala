package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.encoder.Encoder
import com.github.gchudnov.kprojekt.formatter.Folder
import com.github.gchudnov.kprojekt.parser.Parser
import com.github.gchudnov.kprojekt.util.FileOps
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.processor.{ Processor, ProcessorContext, ProcessorSupplier }
import org.apache.kafka.streams.state.{ KeyValueStore, StoreBuilder, Stores }
import org.apache.kafka.streams.{ StreamsBuilder, Topology }
import zio.ZIO
import zio.test.Assertion._
import zio.test._

import scala.jdk.CollectionConverters._

/**
 * ProjektorSpec
 *
 * example:
 *   bloop test lib --only com.github.gchudnov.kprojekt.ProjektorSpec
 *
 *   cat graph.dot | dot -Tpng > graph.png
 */
object ProjektorSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("ProjektorSpec")(
      testM("parsing and rendering a complex topology should produce the expected graphviz output") {
        for {
          input    <- ZIO.fromEither(FileOps.stringFromResource("topologies/complex-topo.log"))
          desc     <- Parser.run(input).provideLayer(Parser.live)
          expected <- ZIO.fromEither(FileOps.stringFromResource("graphs/complex-topo.dot"))
          actual   <- Encoder.encode("complex-topo", desc).provideLayer(Encoder.live).provideLayer(Folder.live)
        } yield assert(actual.trim)(equalTo(expected.trim))
      }
    )
}
