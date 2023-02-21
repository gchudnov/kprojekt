package com.github.gchudnov.kprojekt.output

import com.github.gchudnov.kprojekt.output.Builder
import com.github.gchudnov.kprojekt.output.internal.BasicWriter
import org.apache.kafka.streams.TopologyDescription
import zio._

trait Writer {
  def write(name: String, desc: TopologyDescription): Task[String]
}

object Writer {

    def make: ZLayer[Builder, Nothing, Writer] = 
      ZLayer(for {
        builder <- ZIO.service[Builder]
        service = new BasicWriter(builder)
      } yield service)

}
