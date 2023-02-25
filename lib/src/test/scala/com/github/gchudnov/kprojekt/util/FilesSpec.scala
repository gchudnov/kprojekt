package com.github.gchudnov.kprojekt.util

import java.io.File

import zio.test.Assertion._
import zio.test._

object FilesSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("Files")(
      test("extension can be changed") {
        val f       = new File("/a/b/c.txt")
        val newFile = Files.changeExtension(f, "dat")

        assert(newFile.getAbsolutePath)(equalTo("/a/b/c.dat"))
      },
      test("add extension if previously there was no extension") {
        val f       = new File("/d/e/f")
        val newFile = Files.changeExtension(f, "png")

        assert(newFile.getAbsolutePath)(equalTo("/d/e/f.png"))
      }
    )
}
