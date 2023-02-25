package com.github.gchudnov.kprojekt.output.internal.dot

import com.github.gchudnov.kprojekt.output.Id
import zio._
import zio.test.Assertion._
import zio.test._

object DotLegendSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("DotLegend")(
      test("one-word name is parsed should split it in parts") {
        val input    = Id.processor("KSTREAM-MAPVALUES-0000000002")
        val expected = ("MAPVALUES", Some(2))

        for {
          actual <- ZIO.succeed(DotLegend.buildEntry(input))
        } yield assert(actual)(equalTo(expected))
      },
      test("two-word name is parsed should split it in parts") {
        val input    = Id.processor("KSTREAM-SELECT-KEY-0000000003")
        val expected = ("SELECT.KEY", Some(3))

        for {
          actual <- ZIO.succeed(DotLegend.buildEntry(input))
        } yield assert(actual)(equalTo(expected))
      },
      test("name with suffix is parsed should split it in parts") {
        val input    = Id.processor("KSTREAM-REDUCE-STATE-STORE-0000000007-repartition")
        val expected = ("repartition", Some(7))

        for {
          actual <- ZIO.succeed(DotLegend.buildEntry(input))
        } yield assert(actual)(equalTo(expected))
      },
      test("custom name is parsed should return the original name") {
        val input    = Id.processor("some-custom-name")
        val expected = (input.name, None)

        for {
          actual <- ZIO.succeed(DotLegend.buildEntry(input))
        } yield assert(actual)(equalTo(expected))
      },
      test("long alias should be shortened") {
        val input    = Id.processor("KSTREAM-REDUCE-STATE-STORE-IF-APP-HAS-TOO-MUCH-DATA-0000000007")
        val expected = ("REDUCE.STATE.STORE.IF.APP.HAS.TOO.MUCH.DATA", Some(7))

        for {
          actual <- ZIO.succeed(DotLegend.buildEntry(input))
        } yield assert(actual)(equalTo(expected))
      }
    )
}
