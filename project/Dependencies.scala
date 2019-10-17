import sbt._

object Dependencies {

  object versions {
    val kindProjector = "0.10.3"

    val kafka = "2.3.0"
    val cats = "2.0.0"
    val fastparse = "2.1.3"

    val scalatest = "3.0.8"

    val log4j = "1.2.17"

    val scopt = "4.0.0-RC2"
  }

  // compiler plugins
  private val kindProjector = compilerPlugin(
    "org.typelevel" %% "kind-projector" % versions.kindProjector
  )

  private val compiler = Seq(
    kindProjector
  )

  private val cats = "org.typelevel" %% "cats-core" % versions.cats

  private val kafka = "org.apache.kafka" % "kafka-streams" % versions.kafka
  private val kafkaClients = "org.apache.kafka" % "kafka-clients" % versions.kafka

  private val fastparse = "com.lihaoyi" %% "fastparse" % versions.fastparse

  private val scopt = "com.github.scopt" %% "scopt" % versions.scopt

  private val scalatest = "org.scalatest" %% "scalatest" % versions.scalatest

  val All: Seq[ModuleID] = {
    val compile = Seq(
      cats,
      kafka,
      fastparse,
      scopt
    )
    val test = Seq(
      scalatest,
      kafka,
      kafkaClients
    ) map (_ % "test")
    compile ++ test ++ compiler
  }
}
