package com.github.gchudnov.kprojekt.util

import java.io.File

import zio.test.Assertion._
import zio.test._

/**
 * FileOpsSpec
 *
 * {{{
 * example:
 *   bloop test lib --only com.github.gchudnov.kprojekt.util.FileOpsSpec
 * }}}
 */
object FileOpsSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("FileOpsSpec")(
      test("resource should be non-empty") {
        val errOrData = FileOps.stringFromResource("graphs/fan-out.dot")
        assert(errOrData)(isRight) &&
        assert(errOrData.toTry.get)(isNonEmptyString)
      },
      test("saved resource should be stored") {
        val tmpFile = File.createTempFile("res-", "")
        tmpFile.deleteOnExit()
        val initLen = tmpFile.length()

        val errOrRes = FileOps.saveResource(tmpFile)("images/cylinder.png")

        assert(initLen)(equalTo(0L)) &&
        assert(errOrRes)(isRight) &&
        assert(tmpFile.length())(isGreaterThan(0L))
      },
      test("extension can be changed") {
        val f       = new File("/a/b/c.txt")
        val newFile = FileOps.changeExtension(f, "dat")

        assert(newFile.getAbsolutePath)(equalTo("/a/b/c.dat"))
      },
      test("add extension if previously there was no extension") {
        val f       = new File("/d/e/f")
        val newFile = FileOps.changeExtension(f, "png")

        assert(newFile.getAbsolutePath)(equalTo("/d/e/f.png"))
      }
    )
}
