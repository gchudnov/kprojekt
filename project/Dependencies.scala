import sbt._

object Dependencies {

  object versions {
    val cats          = "2.1.1"
    val fastparse     = "2.3.0"
    val kafka         = "2.5.0"
    val kindProjector = "0.10.3"
    val log4j         = "1.2.17"
    val pureConfig    = "0.12.3"
    val scopt         = "4.0.0-RC2"
    val zio           = "1.0.0-RC19-2"
    val zioLogging    = "0.2.9"
  }

  private val compiler = Seq(
    compilerPlugin("org.typelevel" %% "kind-projector" % versions.kindProjector)
  )

  private val cats            = "org.typelevel"         %% "cats-core"         % versions.cats
  private val fastparse       = "com.lihaoyi"           %% "fastparse"         % versions.fastparse
  private val kafka           = "org.apache.kafka"       % "kafka-streams"     % versions.kafka
  private val kafkaClients    = "org.apache.kafka"       % "kafka-clients"     % versions.kafka
  private val pureConfig      = "com.github.pureconfig" %% "pureconfig"        % versions.pureConfig
  private val scopt           = "com.github.scopt"      %% "scopt"             % versions.scopt
  private val zio             = "dev.zio"               %% "zio"               % versions.zio
  private val zioLogging      = "dev.zio"               %% "zio-logging"       % versions.zioLogging
  private val zioStreams      = "dev.zio"               %% "zio-streams"       % versions.zio
  private val zioTest         = "dev.zio"               %% "zio-test"          % versions.zio
  private val zioTestMagnolia = "dev.zio"               %% "zio-test-magnolia" % versions.zio
  private val zioTestSbt      = "dev.zio"               %% "zio-test-sbt"      % versions.zio

  val All: Seq[ModuleID] = {
    val compile = Seq(
      cats,
      fastparse,
      kafka,
      pureConfig,
      scopt,
      zio,
      zioLogging,
      zioStreams
    )
    val test = Seq(
      kafka,
      kafkaClients,
      zioTest,
      zioTestMagnolia,
      zioTestSbt
    ) map (_ % "test")
    compile ++ test ++ compiler
  }
}
