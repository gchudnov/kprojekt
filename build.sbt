import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.defaultUniversalScript

Global / cancelable := true
Global / scalaVersion := Settings.globalScalaVersion

lazy val allSettings = Settings.sharedSettings ++ Settings.testSettings

lazy val lib = (project in file("lib"))
  .settings(allSettings: _*)
  .settings(
    name := "lib",
    libraryDependencies ++= Dependencies.All
  )

lazy val cli = (project in file("cli"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(lib)
  .settings(allSettings: _*)
  .settings(Settings.assemblySettings)
  .settings(
    name := "kprojekt-cli",
    libraryDependencies ++= Dependencies.All,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.github.gchudnov.kprojekt",
    assembly / mainClass := Some("com.github.gchudnov.kprojekt.Cli"),
    assembly / assemblyOption := (assembly / assemblyOption).value.copy(prependShellScript = Some(defaultUniversalScript(shebang = false))),
    assembly / assemblyOutputPath := new File(s"./target/${name.value}.jar"),
    assembly / assemblyJarName := s"${name.value}"
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
addCommandAlias(
  "upd",
  ";dependencyUpdates; reload plugins; dependencyUpdates; reload return"
)
