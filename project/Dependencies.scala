import sbt._

object Dependencies {

  object versions {
    val fastparse     = "2.3.0"
    val kafka         = "2.6.0"
    val kindProjector = "0.10.3"
    val logback       = "1.2.3"
    val pureConfig    = "0.14.0"
    val scopt         = "4.0.0-RC2"
    val zio           = "1.0.1"
    val zioLogging    = "0.5.2"
  }

  private val compiler = Seq(
    compilerPlugin("org.typelevel" %% "kind-projector" % versions.kindProjector)
  )

  private val fastparse       = "com.lihaoyi"           %% "fastparse"           % versions.fastparse
  private val kafkaStreams    = "org.apache.kafka"      %% "kafka-streams-scala" % versions.kafka
  private val kafkaClients    = "org.apache.kafka"       % "kafka-clients"       % versions.kafka
  private val logback         = "ch.qos.logback"         % "logback-classic"     % versions.logback
  private val pureConfig      = "com.github.pureconfig" %% "pureconfig"          % versions.pureConfig
  private val scopt           = "com.github.scopt"      %% "scopt"               % versions.scopt
  private val zio             = "dev.zio"               %% "zio"                 % versions.zio
  private val zioLogging      = "dev.zio"               %% "zio-logging"         % versions.zioLogging
  private val zioLoggingSlf4j = "dev.zio"               %% "zio-logging-slf4j"   % versions.zioLogging
  private val zioStreams      = "dev.zio"               %% "zio-streams"         % versions.zio
  private val zioTest         = "dev.zio"               %% "zio-test"            % versions.zio
  private val zioTestMagnolia = "dev.zio"               %% "zio-test-magnolia"   % versions.zio
  private val zioTestSbt      = "dev.zio"               %% "zio-test-sbt"        % versions.zio

  val All: Seq[ModuleID] = {
    val compile = Seq(
      fastparse,
      kafkaStreams,
      logback,
      pureConfig,
      scopt,
      zio,
      zioLogging,
      zioLoggingSlf4j,
      zioStreams
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
