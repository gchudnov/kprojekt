package com.github.gchudnov.kprojekt.util

import org.scalatest.{WordSpec, Matchers}
import java.io.File

/**
  * FileOpsSpec
  *
  * example:
  *   bloop test lib --only com.github.gchudnov.kprojekt.util.FileOpsSpec
  *
  */
class FileOpsSpec extends WordSpec with Matchers {

  "FileOps" when {
    "a resource is retrieved" should {
      "be non-empty" in {
        val data = FileOps.stringFromResource("graphs/fan-out.dot")
        data.isEmpty() shouldBe false
      }
    }

    "a resource is saved to a file" should {
      "store it successfully" in {
        val tmpFile = File.createTempFile("res-", "")
        tmpFile.deleteOnExit()
        tmpFile.length() shouldBe 0

        FileOps.saveResource(tmpFile)("images/cylinder.png")
        tmpFile.length() shouldNot be (0)
      }
    }
  }

}
