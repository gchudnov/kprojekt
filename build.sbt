import sbt.Keys.{scalaSource, testFrameworks, _}
import sbt._

autoStartServer := false
Global / cancelable := true

def testFilter(name: String): Boolean = (name endsWith "Spec")

lazy val testSettings = Seq(
  testOptions in Test ++= Seq(Tests.Filter(testFilter)),
)

lazy val allSettings = Settings.shared ++ testSettings

lazy val lib = (project in file("lib"))
  .settings(allSettings: _*)
  .settings(
    name := "lib",
    libraryDependencies ++= Dependencies.All
  )

lazy val cli = (project in file("cli"))
  .dependsOn(lib)
  .settings(allSettings: _*)
  .settings(
    name := "cli",
    libraryDependencies ++= Dependencies.All
  )

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .aggregate(lib, cli)
  .settings(allSettings: _*)
  .settings(
    name := "kprojekt",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.github.gchudnov.kprojekt"
  )
