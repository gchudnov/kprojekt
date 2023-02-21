package com.github.gchudnov.kprojekt.output.internal.dot

import com.github.gchudnov.kprojekt.output.Id
import com.github.gchudnov.kprojekt.output.Edge
import com.github.gchudnov.kprojekt.output.Builder

/**
  * Dot Builder
  *
  */
final class DotBuilder() extends Builder {

  override def build: String = 
    ???

  override def topologyStart(name: String): Builder =
    ???

  override def topologyEnd(): Builder =
    ???

  override def topic(id: Id): Builder = 
    ???

  override def subtopologyStart(name: String): Builder = 
    ???

  override def subtopologyEnd(): Builder = 
    ???

  override def edge(e: Edge): Builder =
    ???

  override def source(id: Id, topics: Iterable[Id]): Builder =
    ???

  override def processor(id: Id, stores: Iterable[Id]): Builder =
    ???

  override def sink(id: Id, topic: Id): Builder =
    ???

  override def storeEdges(edges: Iterable[Edge]): Builder =
    ???

  override def store(id: Id): Builder =
    ???

  override def repository(ns: Iterable[Id]): Builder =
    ???

  override def rank(a: Id, b: Id): Builder =
    ???

}
