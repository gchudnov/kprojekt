import sbt._

object Dependencies {

  object versions {
    val akka = "2.5.25"
    val alpakka = "1.1.1"

    val akkaHttp = "10.1.9"
    val akkaHttpCirce = "1.27.0"

    val cats = "2.0.0"
    val catsEffect = "2.0.0"
    val monocle = "2.0.0"
    val monix = "3.0.0-RC3"
    val meowMtl = "0.3.0-M1"

    val fs2 = "0.10.4"
    val http4s = "0.21.0-M4"
    val sttp = "1.3.2"
    val doobie = "0.5.3"

    val kafka = "2.4.0-SNAPSHOT"
    val embeddedKafka = "2.3.0"

    val circe = "0.12.1"

    val refined = "0.9.9"

    val pureConfig = "0.12.0"
    val config = "1.3.4"

    val scalatest = "3.0.8"
    val scalamock = "4.4.0"
    val scalameter = "0.19"

    val kindProjector = "0.10.3"
    val monadicFor = "0.3.1"

    val logbackClassic = "1.2.3"
    val scalaLogging = "3.9.2"

    val metrics = "4.0.2"

    val awsKafka = "2.7.20"
  }

  // compiler plugins
  private val kindProjector = compilerPlugin(
    "org.typelevel" %% "kind-projector" % versions.kindProjector
  )
  private val monadicFor = compilerPlugin(
    "com.olegpy" %% "better-monadic-for" % versions.monadicFor
  )

  private val compiler = Seq(
    kindProjector,
    monadicFor
  )

  // cats
  private val catsCore = "org.typelevel" %% "cats-core" % versions.cats
  private val catsFree = "org.typelevel" %% "cats-free" % versions.cats
  private val catsEffect = "org.typelevel" %% "cats-effect" % versions.catsEffect

  // cats+
  private val meowMtl = "com.olegpy" %% "meow-mtl" % versions.meowMtl

  // monix -- Asynchronous, Reactive Programming for Scala and Scala.js.
  private val monix = "io.monix" %% "monix" % versions.monix

  // fs2 -- Compositional, streaming I/O library for Scala
  private val fs2 = "co.fs2" %% "fs2-core" % versions.fs2

  // http4s -- A minimal, idiomatic Scala interface for HTTP https://http4s.org/
  private val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % versions.http4s,
    "org.http4s" %% "http4s-circe" % versions.http4s,
    "org.http4s" %% "http4s-blaze-server" % versions.http4s,
    "org.http4s" %% "http4s-blaze-client" % versions.http4s
  )

  // sttp -- The Scala HTTP client you always wanted! https://softwaremill.com/open-source/
  private val sttp = "com.softwaremill.sttp" %% "core" % versions.sttp

  // monocle
  private val monocle = Seq(
    "com.github.julien-truffaut" %% "monocle-core" % versions.monocle,
    "com.github.julien-truffaut" %% "monocle-macro" % versions.monocle
  )

  // akka
  private val akkaActor = "com.typesafe.akka" %% "akka-actor" % versions.akka
  private val akkaContrib = "com.typesafe.akka" %% "akka-contrib" % versions.akka
  private val akkaStream = "com.typesafe.akka" %% "akka-stream" % versions.akka

  private val akkaHttp = "com.typesafe.akka" %% "akka-http" % versions.akkaHttp
  private val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % versions.akkaHttpCirce

  private val alpakka = "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % versions.alpakka

  // akka-testing
  private val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % versions.akka
  private val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % versions.akka
  private val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % versions.akkaHttp

  // testing
  private val scalatest = "org.scalatest" %% "scalatest" % versions.scalatest
  private val scalamock = "org.scalamock" %% "scalamock" % versions.scalamock

  // bench
  private val scalameter = "com.storm-enroute" %% "scalameter" % versions.scalameter
  private val scalameterCode = "com.storm-enroute" %% "scalameter-core" % versions.scalameter // for lightweight inline benchmarking

  // config
  private val config = "com.typesafe" % "config" % versions.config
  private val pureConfig = "com.github.pureconfig" %% "pureconfig" % versions.pureConfig

  // refined data types
  private val refined = "eu.timepit" %% "refined" % versions.refined
//  private val refinedCats = "eu.timepit" %% "refined-cats" % versions.refined

  // doobie -- A principled JDBC layer for Scala.
  private val doobie = "org.tpolecat" %% "doobie-core" % versions.doobie
  private val doobieHikari = "org.tpolecat" %% "doobie-hikari" % versions.doobie // HikariCP transactor.
  private val doobiePg = "org.tpolecat" %% "doobie-postgres" % versions.doobie // Postgres driver 42.2.2 + type mappings.

  // kafka
  private val kafka = Seq(
    "org.apache.kafka" %% "kafka",
    "org.apache.kafka" % "kafka-clients",
    "org.apache.kafka" %% "kafka-streams-scala"
  ).map(_ % versions.kafka)

  // kafka-testing
  private val kafkaStreamsTestUtils = "org.apache.kafka" % "kafka-streams-test-utils" % versions.kafka
  // private val embeddedKafka = "io.github.embeddedkafka" %% "embedded-kafka" % versions.embeddedKafka

  // json
  private val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-parser"
  ).map(_ % versions.circe)

  // logging
  private val logbackClassic = "ch.qos.logback" % "logback-classic" % versions.logbackClassic
  private val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % versions.scalaLogging

  // Metrics
  private val dropwizardCore = "io.dropwizard.metrics" % "metrics-core" % versions.metrics
  private val dropwizardJvm = "io.dropwizard.metrics" % "metrics-jvm" % versions.metrics

  // AWS
  private val awsKafka = "software.amazon.awssdk" % "kafka" % versions.awsKafka

  val All: Seq[ModuleID] = {
    val compile = Seq(
      config,
      pureConfig,
      akkaActor,
      akkaStream,
      alpakka,
      catsCore,
      catsFree,
      catsEffect,
      logbackClassic,
      scalaLogging
    ) ++ kafka ++ circe
    val test = Seq(
      scalatest,
      scalamock,
      scalameter,
      kafkaStreamsTestUtils
    ) ++ kafka map (_ % "test,it,bm")
    compile ++ test ++ compiler
  }
}
