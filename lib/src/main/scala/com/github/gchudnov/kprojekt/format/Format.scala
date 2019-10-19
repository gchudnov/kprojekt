package com.github.gchudnov.kprojekt.format

trait Format[T <: Tag] {

  override def toString(): String

  def topologyStart(name: String): Format[T]
  def topologyEnd(): Format[T]

  def topics(f: (Format[T]) => Format[T]): Format[T] = f(this)
  def topic(name: String): Format[T]

  def subtopologies(f: (Format[T]) => Format[T]): Format[T] = f(this)
  def subtopologyStart(name: String): Format[T]
  def subtopologyEnd(): Format[T]

  def edges(f: (Format[T]) => Format[T]): Format[T] = f(this)
  def edge(fromName: String, toName: String): Format[T]

  def sources(f: (Format[T]) => Format[T]): Format[T] = f(this)
  def source(name: String, topics: Seq[String]): Format[T]

  def processors(f: (Format[T]) => Format[T]): Format[T] = f(this)
  def processor(name: String, stores: Seq[String]): Format[T]

  def sinks(f: (Format[T]) => Format[T]): Format[T] = f(this)
  def sink(name: String, topic: String): Format[T]

  def storeEdges(edges: Seq[(String, String)]): Format[T]
  def stores(f: Format[T] => Format[T]): Format[T] = f(this)
  def store(name: String): Format[T]

  def rank(name1: String, name2: String): Format[T]
}
