import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings

name := "zio-prototype"

version := "0.1"

scalaVersion := "2.12.7"

organization := "org.jinilover"

libraryDependencies ++= compileDependencies ++ itDependencies

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3")
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")

configs(IntegrationTest)
Defaults.itSettings
inConfig(IntegrationTest)(scalafmtConfigSettings)

dependencyClasspath in IntegrationTest := (dependencyClasspath in IntegrationTest).value ++ (exportedProducts in Test).value

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
  "-Ypartial-unification"
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val zioV = "1.0.1"

lazy val compileDependencies = Seq(
  dep("org.tpolecat", "0.8.8", "doobie-core", "doobie-postgres"),
  dep("org.scalaz", "7.2.25", "scalaz-core"),
  dep("com.github.pureconfig", "0.12.3", "pureconfig"),
  dep("dev.zio", zioV, "zio"),
  dep("dev.zio", "2.1.4.0", "zio-interop-cats"),
  dep("com.github.mlangc", "1.0.0-RC21", "zio-interop-log4j2"),
  dep("com.github.mlangc", "1.0.0-RC21-2", "slf4zio"),
  Seq("ch.qos.logback" % "logback-classic" % "1.2.3"),
  Seq("org.flywaydb"   % "flyway-core"     % "5.0.7")
).flatten

lazy val itDependencies = Seq(
  testDep("dev.zio", zioV, "zio-test", "zio-test-sbt"),
  testDep("io.monix", "3.2.1", "monix")
).flatten

def testDep(group: String, version: String, pkgs: String*) =
  dep(group, version, pkgs: _*).map(_ % "it, test").toSeq

def dep(group: String, version: String, pkgs: String*) =
  pkgs.map(group %% _ % version).toSeq