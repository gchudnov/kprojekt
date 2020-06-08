package com.github.gchudnov.kprojekt.naming

import com.github.gchudnov.kprojekt.naming.Namer.Namer
import zio.{ Has, ZLayer }
import zio.test.Assertion.equalTo
import zio.test._

/**
 * NodeNameSpec
 *
 * example:
 *   bloop test lib --only com.github.gchudnov.name.NodeNameSpec
 */
object LiveNamerSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("NodeNameSpec")(
      testM("one-word name is parsed should correctly split it in parts") {
        val input    = "KSTREAM-MAPVALUES-0000000002"
        val expected = NodeName(id = Some(2), alias = "MAPVALUES", original = input)

        for {
          actual <- Namer.name(input).provideLayer(defaultEnv)
        } yield assert(actual)(equalTo(expected))
      },
      testM("two-word name is parsed should correctly split it in parts") {
        val input    = "KSTREAM-SELECT-KEY-0000000003"
        val expected = NodeName(id = Some(3), alias = "SELECT.KEY", original = input)

        for {
          actual <- Namer.name(input).provideLayer(defaultEnv)
        } yield assert(actual)(equalTo(expected))
      },
      testM("name with wuffix is parsed should correctly split it in parts") {
        val input    = "KSTREAM-REDUCE-STATE-STORE-0000000007-repartition"
        val expected = NodeName(id = Some(7), alias = "repartition", original = input)

        for {
          actual <- Namer.name(input).provideLayer(defaultEnv)
        } yield assert(actual)(equalTo(expected))
      },
      testM("custom name is parsed should return the original name") {
        val input    = "some-custom-name"
        val expected = NodeName(input)

        for {
          actual <- Namer.name(input).provideLayer(defaultEnv)
        } yield assert(actual)(equalTo(expected))
      },
      testM("long alias should be shortened") {
        val input    = "KSTREAM-REDUCE-STATE-STORE-IF-APP-HAS-TOO-MUCH-DATA-0000000007"
        val expected = NodeName(id = Some(7), alias = "R.S.S.I.A.H.T.M.DATA", original = input)

        for {
          actual <- Namer.name(input).provideLayer(defaultEnv)
        } yield assert(actual)(equalTo(expected))
      }
    )

  private val defaultNameConfig = NameConfig(maxLenWithoutShortening = 12, separator = ".")

  private val defaultEnv: ZLayer[Any, Nothing, Has[Namer.Service]] =
    withEnv(defaultNameConfig)

  private def withEnv(nameConfig: NameConfig): ZLayer[Any, Nothing, Namer] = {
    val nameConfigEnv = ZLayer.succeedMany(nameConfig)

    val nameEnv = (nameConfigEnv >>> Namer.live)

    nameEnv
  }
}
