package com.github.gchudnov.kprojekt.formatter

import com.github.gchudnov.kprojekt.formatter.dot.{ DotConfig, DotFolder, DotFolderState }
import com.github.gchudnov.kprojekt.ids.NodeId
import com.github.gchudnov.kprojekt.naming.Namer
import zio.{ Has, ZLayer }

object Folder {
  type Folder        = Has[Folder.Service]
  type ServiceMapper = Folder.Service => Folder.Service

  trait Service {
    def toString: String

    def topologyStart(name: String): Service
    def topologyEnd(): Service

    def topics(f: ServiceMapper): Service = f(this)
    def topic(id: NodeId): Service

    def subtopologies(f: ServiceMapper): Service = f(this)
    def subtopologyStart(name: String): Service
    def subtopologyEnd(): Service

    def edges(f: ServiceMapper): Service = f(this)
    def edge(from: NodeId, to: NodeId): Service

    def sources(f: ServiceMapper): Service = f(this)
    def source(id: NodeId, topics: Seq[NodeId]): Service

    def processors(f: ServiceMapper): Service = f(this)
    def processor(id: NodeId, stores: Seq[NodeId]): Service

    def sinks(f: ServiceMapper): Service = f(this)
    def sink(id: NodeId, topic: NodeId): Service

    def storeEdges(edges: Seq[(NodeId, NodeId)]): Service
    def stores(f: ServiceMapper): Service = f(this)
    def store(id: NodeId): Service

    def repository(ns: Seq[NodeId]): Service

    def rank(a: NodeId, b: NodeId): Service
  }

  val any: ZLayer[Folder, Nothing, Folder] =
    ZLayer.requires[Folder]

  val live: ZLayer[Has[DotConfig] with Has[Namer.Service], Nothing, Folder] =
    ZLayer.fromServices[DotConfig, Namer.Service, Folder.Service]((config: DotConfig, namer: Namer.Service) =>
      new DotFolder(config = config, namer = namer, state = DotFolderState())
    )
}
