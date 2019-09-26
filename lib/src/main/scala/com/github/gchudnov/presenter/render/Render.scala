package com.github.gchudnov.presenter.render

trait Render[A] {

  def get: A

  def topologyStart(name: String): Render[A]
  def topologyEnd(): Render[A]

  def topics(f: (Render[A]) => Render[A]): Render[A] = f(this)
  def topic(t: String): Render[A]

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
}
