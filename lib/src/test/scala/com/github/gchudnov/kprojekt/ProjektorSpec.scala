package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.input.Parser
import zio._
import zio.test.Assertion._
import zio.test.{ TestEnvironment, _ }
import com.github.gchudnov.kprojekt.output.Builder
import com.github.gchudnov.kprojekt.output.Writer
import com.github.gchudnov.kprojekt.util.Resources

object ProjektorSpec extends ZIOSpecDefault {

  private val dotEnv = makeDotEnv

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ProjektorSpec")(
      test("DOT - parsing and write a complex topology") {
        val program = for {
          input    <- ZIO.fromEither(Resources.lines("topologies/complex-topo-1.log"))
          desc     <- Parser.parse(input)
          expected <- ZIO.fromEither(Resources.lines("graphs/complex-topo-1.dot"))
          actual   <- Writer.write("complex-topo-1", desc)
        } yield assert(actual.trim)(equalTo(expected.trim))

        program.provideLayer(dotEnv)
      },
      test("DOT - parsing a complex topology should produce no duplicates for sources") {
        val program = for {
          input    <- ZIO.fromEither(Resources.lines("topologies/complex-topo-2.log"))
          desc     <- Parser.parse(input)
          expected <- ZIO.fromEither(Resources.lines("graphs/complex-topo-2.dot"))
          actual   <- Writer.write("complex-topo-2", desc)
        } yield assert(actual.trim)(equalTo(expected.trim))

        program.provideLayer(dotEnv)
      }
    )

  def makeDotEnv: ULayer[Parser with Writer] = {
    val parserEnv = Parser.make
    val writerEnv = Builder.dot >>> Writer.make

    val env = parserEnv ++ writerEnv

    env
  }
}
