import sbt._

object Dependencies {

  object versions {
    val cats          = "2.1.1"
    val fastparse     = "2.3.0"
    val kafka         = "2.5.0"
    val kindProjector = "0.10.3"
    val log4j         = "1.2.17"
    val scalatest     = "3.1.2"
    val scopt         = "4.0.0-RC2"
  }

  private val compiler = Seq(
    compilerPlugin("org.typelevel" %% "kind-projector" % versions.kindProjector)
  )

  private val cats         = "org.typelevel"    %% "cats-core"     % versions.cats
  private val fastparse    = "com.lihaoyi"      %% "fastparse"     % versions.fastparse
  private val kafka        = "org.apache.kafka"  % "kafka-streams" % versions.kafka
  private val kafkaClients = "org.apache.kafka"  % "kafka-clients" % versions.kafka
  private val scalatest    = "org.scalatest"    %% "scalatest"     % versions.scalatest
  private val scopt        = "com.github.scopt" %% "scopt"         % versions.scopt

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
