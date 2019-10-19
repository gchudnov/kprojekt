package com.github.gchudnov.kprojekt.format

trait Format[A] {

  def get: A

  def topologyStart(name: String): Format[A]
  def topologyEnd(): Format[A]

  def topics(f: (Format[A]) => Format[A]): Format[A] = f(this)
  def topic(name: String): Format[A]

  def subtopologies(f: (Format[A]) => Format[A]): Format[A] = f(this)
  def subtopologyStart(name: String): Format[A]
  def subtopologyEnd(): Format[A]

  def edges(f: (Format[A]) => Format[A]): Format[A] = f(this)
  def edge(fromName: String, toName: String): Format[A]

  def sources(f: (Format[A]) => Format[A]): Format[A] = f(this)
  def source(name: String, topics: Seq[String]): Format[A]

  def processors(f: (Format[A]) => Format[A]): Format[A] = f(this)
  def processor(name: String, stores: Seq[String]): Format[A]

  def sinks(f: (Format[A]) => Format[A]): Format[A] = f(this)
  def sink(name: String, topic: String): Format[A]

  def storeEdges(edges: Seq[(String, String)]): Format[A]
  def stores(f: Format[A] => Format[A]): Format[A] = f(this)
  def store(name: String): Format[A]

  def rank(name1: String, name2: String): Format[A]
}
