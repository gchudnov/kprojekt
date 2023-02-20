import sbt.Keys._
import sbt._
import sbt.Package._

object Settings {
  private val scalaV = "2.13.10"

  private val sharedScalacOptions = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8",                         // Specify character encoding used by source files.
    "-explaintypes",                 // Explain type errors in more detail.
    "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds",         // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-language:postfixOps",          // Enable postfixOps
    "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
    "-Xlint",
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ymacro-annotations",
    "-Xcheckinit"
  )

  val globalScalaVersion: String = scalaV

  val sharedResolvers: Vector[MavenRepository] = Seq(
    Resolver.jcenterRepo,
    Resolver.mavenLocal
  ).toVector

  val sharedSettings: Seq[Setting[_]] = Seq(
    scalacOptions ++= sharedScalacOptions,
    scalaVersion         := scalaV,
    ThisBuild / turbo    := true,
    cancelable in Global := true,
    fork in Global       := true,
    resolvers            := Resolver.combineDefaultResolvers(sharedResolvers),
    compileOrder         := CompileOrder.JavaThenScala,
    organization         := "com.github.gchudnov"
  )

  def testFilter(name: String): Boolean = (name endsWith "Spec")

  val testSettings: Seq[Setting[_]] = Seq(
    Test / testOptions ++= Seq(Tests.Filter(testFilter)),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
}
