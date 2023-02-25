import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.defaultUniversalScript

Global / cancelable   := true
Global / scalaVersion := Settings.globalScalaVersion

lazy val allSettings = Settings.sharedSettings ++ Settings.testSettings

lazy val lib = (project in file("lib"))
  .settings(allSettings: _*)
  .disablePlugins(AssemblyPlugin)
  .settings(
    name := "lib",
    libraryDependencies ++= Dependencies.Lib
  )

lazy val cli = (project in file("cli"))
  .dependsOn(lib)
  .enablePlugins(BuildInfoPlugin, JavaAppPackaging, GraalVMNativeImagePlugin)
  .settings(allSettings: _*)
  .settings(Settings.assemblySettings)
  .settings(
    name := "kprojekt-cli",
    libraryDependencies ++= Dependencies.Cli,
    buildInfoKeys                 := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage              := "com.github.gchudnov.kprojekt",
    assembly / mainClass          := Some("com.github.gchudnov.kprojekt.Cli"),
    assembly / assemblyOption     := (assembly / assemblyOption).value.withPrependShellScript(Some(defaultUniversalScript(shebang = true))),
    assembly / assemblyOutputPath := new File(s"./target/${name.value}"),
    assembly / assemblyJarName    := s"${name.value}",
    graalVMNativeImageOptions ++= Seq("--no-fallback", "--verbose") // NOTE: add --dry-run to investigate the build
  )

lazy val root = (project in file("."))
  .aggregate(lib, cli)
  .disablePlugins(AssemblyPlugin)
  .settings(allSettings: _*)
  .settings(
    name := "kprojekt"
  )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
addCommandAlias("plg", "; reload plugins ; libraryDependencies ; reload return")
// NOTE: to use version check for plugins, add to the meta-project (/project/project) sbt-updates.sbt with "sbt-updates" plugin as well.
addCommandAlias("upd", ";dependencyUpdates; reload plugins; dependencyUpdates; reload return")
