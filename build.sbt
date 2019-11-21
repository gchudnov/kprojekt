import sbt._
import sbt.Keys.{scalaSource, testFrameworks, _}
import sbtassembly.AssemblyPlugin.defaultUniversalScript

autoStartServer := false
Global / cancelable := true

def testFilter(name: String): Boolean = (name endsWith "Spec")

lazy val testSettings = Seq(
  testOptions in Test ++= Seq(Tests.Filter(testFilter))
)

lazy val allSettings = Settings.shared ++ testSettings

lazy val lib = (project in file("lib"))
  .settings(allSettings: _*)
  .settings(
    name := "lib",
    libraryDependencies ++= Dependencies.All
  )

lazy val cli = (project in file("cli"))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
  .dependsOn(lib)
  .settings(allSettings: _*)
  .settings(
    name := "kprojekt-cli",
    libraryDependencies ++= Dependencies.All,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.github.gchudnov.kprojekt",
    graalVMNativeImageGraalVersion := Some("19.2.1"),
    graalVMNativeImageOptions ++= Seq(
      "--initialize-at-build-time",
      "--no-fallback",
      "--allow-incomplete-classpath"
    ),
    mainClass in assembly := Some("com.github.gchudnov.kprojekt.Cli"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultUniversalScript(shebang = false))),
    assemblyJarName in assembly := s"${name.value}"
  )

lazy val root = (project in file("."))
  .aggregate(lib, cli)
  .settings(allSettings: _*)
  .settings(
    name := "kprojekt"
  )
