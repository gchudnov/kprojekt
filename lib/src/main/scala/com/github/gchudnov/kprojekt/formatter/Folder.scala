package com.github.gchudnov.kprojekt.formatter

import com.github.gchudnov.kprojekt.formatter.dot.{ DotConfig, DotFolder }
import zio.{ Has, ZLayer }

object Folder {
  type Folder        = Has[Folder.Service]
  type ServiceMapper = Folder.Service => Folder.Service

  trait Service {
    def toString: String

    def topologyStart(name: String): Service
    def topologyEnd(): Service

    def topics(f: ServiceMapper): Service = f(this)
    def topic(name: String): Service

    def subtopologies(f: ServiceMapper): Service = f(this)
    def subtopologyStart(name: String): Service
    def subtopologyEnd(): Service

    def edges(f: ServiceMapper): Service = f(this)
    def edge(fromName: String, toName: String): Service

    def sources(f: ServiceMapper): Service = f(this)
    def source(name: String, topics: Seq[String]): Service

    def processors(f: ServiceMapper): Service = f(this)
    def processor(name: String, stores: Seq[String]): Service

    def sinks(f: ServiceMapper): Service = f(this)
    def sink(name: String, topic: String): Service

    def storeEdges(edges: Seq[(String, String)]): Service
    def stores(f: ServiceMapper): Service = f(this)
    def store(name: String): Service

    def rank(name1: String, name2: String): Service
  }

  val any: ZLayer[Folder, Nothing, Folder] =
    ZLayer.requires[Folder]

  val live: ZLayer[DotConfig, Nothing, Folder] =
    ZLayer.fromFunction(config => new DotFolder(config))
}
