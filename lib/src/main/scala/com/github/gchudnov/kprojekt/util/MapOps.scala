package com.github.gchudnov.kprojekt.util

object MapOps {

  private def combine(a: Map[String, List[String]], b: Map[String, List[String]]): Map[String, List[String]] =
    a ++ b.map { case (k, v) => k -> (v ++ a.getOrElse(k, List.empty[String])) }

  implicit class RichMap(a: Map[String, List[String]]) {
    def |+|(b: Map[String, List[String]]): Map[String, List[String]] =
      combine(a, b)
  }

}
