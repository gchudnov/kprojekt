import sbt.Keys.{scalaSource, testFrameworks, _}
import sbt._

autoStartServer := false

def itFilter(name: String): Boolean = name endsWith "ITSpec"
def testFilter(name: String): Boolean = (name endsWith "Spec") && !itFilter(name)

val IntegrationTestTag = Tags.Tag("it")

Global / concurrentRestrictions += Tags.limit(IntegrationTestTag, 1)

lazy val testSettings = Seq(
  testOptions in Test ++= Seq(Tests.Filter(testFilter)),
)

lazy val itSettings = inConfig(IntegrationTest)(Defaults.testTasks) ++
  Defaults.itSettings ++
  Seq(
    fork in IntegrationTest := true,
    parallelExecution in IntegrationTest := true,            // NOTE: parallelExecution controls whether tests are mapped to separate tasks
    tags in IntegrationTest := Seq((IntegrationTestTag, 1)), // NOTE: to restrict the number of concurrently executing given tests in all projects
    testOptions in IntegrationTest := Seq(Tests.Filter(itFilter)),
    scalaSource in IntegrationTest := baseDirectory.value / "src/test/scala",
    javaSource in IntegrationTest := baseDirectory.value / "src/test/java",
    resourceDirectory in IntegrationTest := baseDirectory.value / "src/test/resources",
  )

lazy val allConfigs = Seq(IntegrationTest)
lazy val allSettings = Settings.shared ++ testSettings ++ itSettings

lazy val root = (project in file("."))
  .configs(allConfigs: _*)
  .settings(allSettings: _*)
  .settings(
    name := "kprojekt",
    libraryDependencies ++= Dependencies.All,
    resolvers ++= Seq(Resolver.jcenterRepo, Resolver.mavenLocal),
  )
