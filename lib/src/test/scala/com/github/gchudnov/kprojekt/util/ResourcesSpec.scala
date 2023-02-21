package com.github.gchudnov.kprojekt.util

import java.io.File

import zio.test.Assertion._
import zio.test._


object ResourcesSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("Resources")(
      test("saved resource should be stored") {
        val tmpFile = File.createTempFile("res-", "")
        tmpFile.deleteOnExit()
        val initLen = tmpFile.length()

        val errOrRes = Resources.saveResource(tmpFile, "graphs/complex-topo-1.dot")

        assert(initLen)(equalTo(0L)) &&
        assert(errOrRes)(isRight) &&
        assert(tmpFile.length())(isGreaterThan(0L))
      },
      test("resource should be non-empty") {
        val errOrData = Resources.linesFromResource("graphs/fan-out.dot")
        assert(errOrData)(isRight) &&
        assert(errOrData.toTry.get)(isNonEmptyString)
      },
    )
}
