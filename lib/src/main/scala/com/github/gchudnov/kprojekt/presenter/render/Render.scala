package com.github.gchudnov.kprojekt.presenter.render

trait Render[A] {

  def get: A

  def topologyStart(name: String): Render[A]
  def topologyEnd(): Render[A]

  def topics(f: (Render[A]) => Render[A]): Render[A] = f(this)
  def topic(name: String): Render[A]

  def subtopologies(f: (Render[A]) => Render[A]): Render[A] = f(this)
  def subtopologyStart(name: String): Render[A]
  def subtopologyEnd(): Render[A]

  def edges(f: (Render[A]) => Render[A]): Render[A] = f(this)
  def edge(fromName: String, toName: String): Render[A]

  def sources(f: (Render[A]) => Render[A]): Render[A] = f(this)
  def source(name: String, topics: Seq[String]): Render[A]

  def processors(f: (Render[A]) => Render[A]): Render[A] = f(this)
  def processor(name: String, stores: Seq[String]): Render[A]

  def sinks(f: (Render[A]) => Render[A]): Render[A] = f(this)
  def sink(name: String, topic: String): Render[A]

  def storeEdges(edges: Seq[(String, String)]): Render[A]
  def stores(f: Render[A] => Render[A]): Render[A] = f(this)
  def store(name: String): Render[A]

  def rank(name1: String, name2: String): Render[A]
}
