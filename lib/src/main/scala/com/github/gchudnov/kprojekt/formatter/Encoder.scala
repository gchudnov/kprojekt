package com.github.gchudnov.kprojekt.formatter

/**
 * Encode topology to a string.
 */
trait Encoder[T <: Tag] {

  def toString: String

  def topologyStart(name: String): Encoder[T]
  def topologyEnd(): Encoder[T]

  def topics(f: (Encoder[T]) => Encoder[T]): Encoder[T] = f(this)
  def topic(name: String): Encoder[T]

  def subtopologies(f: (Encoder[T]) => Encoder[T]): Encoder[T] = f(this)
  def subtopologyStart(name: String): Encoder[T]
  def subtopologyEnd(): Encoder[T]

  def edges(f: (Encoder[T]) => Encoder[T]): Encoder[T] = f(this)
  def edge(fromName: String, toName: String): Encoder[T]

  def sources(f: (Encoder[T]) => Encoder[T]): Encoder[T] = f(this)
  def source(name: String, topics: Seq[String]): Encoder[T]

  def processors(f: (Encoder[T]) => Encoder[T]): Encoder[T] = f(this)
  def processor(name: String, stores: Seq[String]): Encoder[T]

  def sinks(f: (Encoder[T]) => Encoder[T]): Encoder[T] = f(this)
  def sink(name: String, topic: String): Encoder[T]

  def storeEdges(edges: Seq[(String, String)]): Encoder[T]
  def stores(f: Encoder[T] => Encoder[T]): Encoder[T] = f(this)
  def store(name: String): Encoder[T]

  def rank(name1: String, name2: String): Encoder[T]
}
