package com.github.gchudnov.kprojekt.util

object Maps {

  private def combine[T](a: Map[T, List[T]], b: Map[T, List[T]]): Map[T, List[T]] =
    a ++ b.map { case (k, v) => k -> (v ++ a.getOrElse(k, List.empty[T])) }

  implicit class RichMap[T](val a: Map[T, List[T]]) extends AnyVal {
    def |+|(b: Map[T, List[T]]): Map[T, List[T]] =
      combine(a, b)
  }

}
