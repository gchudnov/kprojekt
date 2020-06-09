import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.defaultUniversalScript

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
    mainClass in assembly := Some("com.github.gchudnov.kprojekt.Cli"),
//    assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultUniversalScript(shebang = false))),
    assemblyOutputPath in assembly := new File(s"./target/${name.value}.jar"),
    assemblyJarName in assembly := s"${name.value}"
  )

lazy val root = (project in file("."))
  .aggregate(lib, cli)
  .settings(allSettings: _*)
  .settings(
    name := "kprojekt"
  )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
