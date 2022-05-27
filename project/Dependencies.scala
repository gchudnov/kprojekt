import sbt._

object Dependencies {

  object versions {
    val fastparse      = "2.3.3"
    val kafka          = "3.2.0"
    val kindProjector  = "0.10.3"
    val logbackClassic = "1.2.11"
    val scopt          = "4.0.1"
    val zio            = "2.0.0-RC6"
    val zioConfig      = "3.0.0-RC9"
    val zioLogging     = "2.0.0-RC10"
  }

  private val compiler = Seq(
    compilerPlugin("org.typelevel" %% "kind-projector" % versions.kindProjector)
  )

  private val zio        = "dev.zio" %% "zio"         % versions.zio
  private val zioStreams = "dev.zio" %% "zio-streams" % versions.zio

  private val zioConfig         = "dev.zio" %% "zio-config"          % versions.zioConfig
  private val zioConfigMagnolia = "dev.zio" %% "zio-config-magnolia" % versions.zioConfig
  private val zioConfigTypesafe = "dev.zio" %% "zio-config-typesafe" % versions.zioConfig

  private val zioLogging      = "dev.zio"       %% "zio-logging"       % versions.zioLogging
  private val zioLoggingSlf4j = "dev.zio"       %% "zio-logging-slf4j" % versions.zioLogging
  private val logbackClassic  = "ch.qos.logback" % "logback-classic"   % versions.logbackClassic

  private val zioTest         = "dev.zio" %% "zio-test"          % versions.zio
  private val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % versions.zio
  private val zioTestSbt      = "dev.zio" %% "zio-test-sbt"      % versions.zio

  private val scopt     = "com.github.scopt" %% "scopt"     % versions.scopt
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
      // config
      zioConfig,
      zioConfigMagnolia,
      zioConfigTypesafe,
      // log
      zioLogging,
      zioLoggingSlf4j,
      logbackClassic,
      // util
      scopt
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
