import sbt.Keys._
import sbt._

Global / cancelable   := true
Global / scalaVersion := Settings.globalScalaVersion

lazy val allSettings = Settings.sharedSettings ++ Settings.testSettings

lazy val lib = (project in file("lib"))
  .settings(allSettings: _*)
  .settings(
    name := "lib",
    libraryDependencies ++= Dependencies.Lib
  )

lazy val cli = (project in file("cli"))
  .dependsOn(lib)
  .enablePlugins(BuildInfoPlugin, JavaAppPackaging, GraalVMNativeImagePlugin)
  .settings(allSettings: _*)
  .settings(
    name := "kprojekt-cli",
    libraryDependencies ++= Dependencies.Cli,
    buildInfoKeys    := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.github.gchudnov.kprojekt",
    graalVMNativeImageOptions ++= Seq("--no-fallback", "--verbose") // NOTE: add --dry-run to investigate the build
  )

lazy val root = (project in file("."))
  .aggregate(lib, cli)
  .settings(allSettings: _*)
  .settings(
    name := "kprojekt"
  )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
addCommandAlias("plgV", "; reload plugins ; libraryDependencies ; reload return")
addCommandAlias("upd", ";dependencyUpdates; reload plugins; dependencyUpdates; reload return")
