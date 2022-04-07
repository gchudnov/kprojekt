import sbt._

object Dependencies {

  object versions {
    val fastparse     = "2.3.3"
    val kafka         = "3.1.0"
    val kindProjector = "0.10.3"
    val logback       = "1.2.11"
    val scopt         = "4.0.1"
    val zio           = "2.0.0-RC4"
    val zioConfig     = "3.0.0-RC7"
  }

  private val compiler = Seq(
    compilerPlugin("org.typelevel" %% "kind-projector" % versions.kindProjector)
  )

  private val fastparse    = "com.lihaoyi"           %% "fastparse"           % versions.fastparse
  private val kafkaStreams = "org.apache.kafka"      %% "kafka-streams-scala" % versions.kafka
  private val kafkaClients = "org.apache.kafka"       % "kafka-clients"       % versions.kafka
  private val logback      = "ch.qos.logback"         % "logback-classic"     % versions.logback
  private val scopt        = "com.github.scopt"      %% "scopt"               % versions.scopt

  private val zioConfig         = "dev.zio" %% "zio-config" % versions.zioConfig
  private val zioConfigMagnolia = "dev.zio" %% "zio-config-magnolia" % versions.zioConfig
  private val zioConfigTypesafe = "dev.zio" %% "zio-config-typesafe" % versions.zioConfig

  private val zio             = "dev.zio" %% "zio"               % versions.zio
  private val zioStreams      = "dev.zio" %% "zio-streams"       % versions.zio
  private val zioTest         = "dev.zio" %% "zio-test"          % versions.zio
  private val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % versions.zio
  private val zioTestSbt      = "dev.zio" %% "zio-test-sbt"      % versions.zio

  // TODO: split for Cli and Lib
  val All: Seq[ModuleID] = {
    val compile = Seq(
      // zio
      zio,
      zioStreams,
      // config
      zioConfig,
      zioConfigMagnolia,
      zioConfigTypesafe,
      // options
      scopt,
      // util
      fastparse,
      kafkaStreams,
      logback
    )
    val test = Seq(
      kafkaStreams,
      kafkaClients,
      zioTest,
      zioTestMagnolia,
      zioTestSbt
    ) map (_ % "test")
    compile ++ test ++ compiler
  }
}
