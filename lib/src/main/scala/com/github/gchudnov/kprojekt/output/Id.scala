package com.github.gchudnov.kprojekt.output

import scala.math.Ordering

final case class Id(
  name: String,
  uid: String
)

object Id {

  implicit val idOrdering: Ordering[Id] =
    (x: Id, y: Id) => x.uid.compare(y.uid)

  def topic(name: String): Id =
    Id(name = name, uid = s"t:$name")

  def source(name: String): Id =
    Id(name = name, uid = s"s:$name")

  def processor(name: String): Id =
    Id(name = name, uid = s"p:$name")

  def sink(name: String): Id =
    Id(name = name, uid = s"k:$name")

  def store(name: String): Id =
    Id(name = name, uid = s"r:$name")

}
