package com.github.gchudnov.kprojekt.util

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import scala.util.Using

object FileOps {

  def save(file: File)(data: String) = {
    Using(new BufferedWriter(new FileWriter(file))) { writer =>
      writer.write(data)
    }
  }

}
