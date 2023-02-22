package com.github.gchudnov.kprojekt.output

import com.github.gchudnov.kprojekt.output.internal.dot.DotBuilder
import zio._

trait Builder {

  def build: String

  def legend(ns: Iterable[Id]): Builder

  def topologyStart(name: String): Builder
  def topologyEnd(): Builder

  def topics(f: Builder => Builder): Builder = f(this)
  def topic(id: Id): Builder

  def subtopologies(f: Builder => Builder): Builder = f(this)
  def subtopologyStart(name: String): Builder
  def subtopologyEnd(): Builder

  def edges(f: Builder => Builder): Builder = f(this)
  def edge(e: Edge): Builder

  def sources(f: Builder => Builder): Builder = f(this)
  def source(id: Id, topics: Iterable[Id]): Builder

  def processors(f: Builder => Builder): Builder = f(this)
  def processor(id: Id, stores: Iterable[Id]): Builder

  def sinks(f: Builder => Builder): Builder = f(this)
  def sink(id: Id, topic: Id): Builder

  def storeEdges(edges: Iterable[Edge]): Builder
  def stores(f: Builder => Builder): Builder = f(this)
  def store(id: Id): Builder

  def rank(a: Id, b: Id): Builder

}

object Builder {

  def dot: ZLayer[Any, Nothing, Builder] = 
    ZLayer.succeed(DotBuilder())

}
