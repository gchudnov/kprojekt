import sbt._

object Dependencies {

  object versions {
    val fastparse     = "2.3.3"
    val kafka         = "3.4.0"
    val kindProjector = "0.10.3"
    val zio           = "2.0.9"
    val zioCli        = "0.4.0"
  }

  private val compiler = Seq(
    compilerPlugin("org.typelevel" %% "kind-projector" % versions.kindProjector)
  )

  private val zio        = "dev.zio" %% "zio"         % versions.zio
  private val zioStreams = "dev.zio" %% "zio-streams" % versions.zio

  private val zioCli = "dev.zio" %% "zio-cli" % versions.zioCli

  private val zioTest         = "dev.zio" %% "zio-test"          % versions.zio
  private val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % versions.zio
  private val zioTestSbt      = "dev.zio" %% "zio-test-sbt"      % versions.zio

  private val fastparse = "com.lihaoyi"      %% "fastparse" % versions.fastparse

  private val kafkaStreams = "org.apache.kafka" %% "kafka-streams-scala" % versions.kafka
  private val kafkaClients = "org.apache.kafka"  % "kafka-clients"       % versions.kafka

  val Lib: Seq[ModuleID] = {
    val compile = Seq(
      // zio
      zio,
      zioStreams,
      kafkaStreams,
      // util
      fastparse
    )
    val test = Seq(
      zioTest,
      zioTestMagnolia,
      zioTestSbt
    ) map (_ % "test")
    compile ++ test ++ compiler
  }

  val Cli: Seq[ModuleID] = {
    val compile = Seq(
      // zio
      zio,
      zioStreams,
      // util
      zioCli
    )
    val test = Seq(
      kafkaClients,
      zioTest,
      zioTestMagnolia,
      zioTestSbt
    ) map (_ % "test")
    compile ++ test ++ compiler
  }
}
