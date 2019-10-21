package com.github.gchudnov.kprojekt.util

import org.scalatest.{WordSpec, Matchers, EitherValues}
import java.io.File

/**
  * FileOpsSpec
  *
  * example:
  *   bloop test lib --only com.github.gchudnov.kprojekt.util.FileOpsSpec
  *
  */
class FileOpsSpec extends WordSpec with Matchers with EitherValues {

  "FileOps" when {
    "a resource is retrieved" should {
      "be non-empty" in {
        val errOrData = FileOps.stringFromResource("graphs/fan-out.dot")
        errOrData.isRight shouldBe true
        errOrData.foreach(data => {
          data.isEmpty() shouldBe false
        })
      }
    }

    "a resource is saved to a file" should {
      "store it successfully" in {
        val tmpFile = File.createTempFile("res-", "")
        tmpFile.deleteOnExit()
        tmpFile.length() shouldBe 0

        val errOrRes = FileOps.saveResource(tmpFile)("images/cylinder.png")
        errOrRes.isRight shouldBe true
        tmpFile.length() shouldNot be(0)
      }
    }

    "file extension is changed" should {
      "replace it if previously there was an extension" in {
        val f = new File("/a/b/c.txt")
        val newFile = FileOps.changeExtension(f, "dat")

        newFile.getAbsolutePath() shouldBe "/a/b/c.dat"
      }

      "replace it if previously there was no extension" in {
        val f = new File("/d/e/f")
        val newFile = FileOps.changeExtension(f, "png")

        newFile.getAbsolutePath() shouldBe "/d/e/f.png"
      }
    }
  }

}
