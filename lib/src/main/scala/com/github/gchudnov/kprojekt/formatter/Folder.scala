package com.github.gchudnov.kprojekt.formatter

import com.github.gchudnov.kprojekt.formatter.Folder.ServiceMapper
import com.github.gchudnov.kprojekt.ids.NodeId

trait Folder {
  def toString: String

  def topologyStart(name: String): Folder
  def topologyEnd(): Folder

  def topics(f: ServiceMapper): Folder = f(this)
  def topic(id: NodeId): Folder

  def subtopologies(f: ServiceMapper): Folder = f(this)
  def subtopologyStart(name: String): Folder
  def subtopologyEnd(): Folder

  def edges(f: ServiceMapper): Folder = f(this)
  def edge(from: NodeId, to: NodeId): Folder

  def sources(f: ServiceMapper): Folder = f(this)
  def source(id: NodeId, topics: Seq[NodeId]): Folder

  def processors(f: ServiceMapper): Folder = f(this)
  def processor(id: NodeId, stores: Seq[NodeId]): Folder

  def sinks(f: ServiceMapper): Folder = f(this)
  def sink(id: NodeId, topic: NodeId): Folder

  def storeEdges(edges: Seq[(NodeId, NodeId)]): Folder
  def stores(f: ServiceMapper): Folder = f(this)
  def store(id: NodeId): Folder

  def repository(ns: Seq[NodeId]): Folder

  def rank(a: NodeId, b: NodeId): Folder
}

object Folder {
  type ServiceMapper = Folder => Folder
}
