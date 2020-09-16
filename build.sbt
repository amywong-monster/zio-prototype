import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings

name := "zio-prototype"

version := "0.1"

scalaVersion := "2.12.7"

organization := "org.jinilover"

lazy val doobieV = "0.8.8"
lazy val zioV = "1.0.1"

libraryDependencies += "org.tpolecat" %% "doobie-core" % doobieV
libraryDependencies += "org.tpolecat" %% "doobie-postgres" % doobieV
libraryDependencies +=  "org.scalaz" %% "scalaz-core" % "7.2.25"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.12.3"
libraryDependencies += "dev.zio" %% "zio" %  zioV
libraryDependencies += "dev.zio" %% "zio-interop-cats" % "2.1.4.0"
libraryDependencies +=   "com.github.mlangc" %% "zio-interop-log4j2" % "1.0.0-RC21"
libraryDependencies += "com.github.mlangc" %% "slf4zio" % "1.0.0-RC21-2"
libraryDependencies +=  "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies +=  "org.flywaydb"   % "flyway-core"     % "5.0.7"

libraryDependencies ++= itDependencies

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

lazy val itDependencies = Seq(
  testDep("dev.zio", zioV, "zio-test", "zio-test-sbt"),
  testDep("io.monix", "3.2.1", "monix")
).flatten

def testDep(group: String, version: String, pkgs: String*) =
  dep(group, version, pkgs: _*).map(_ % "it, test").toSeq

def dep(group: String, version: String, pkgs: String*) =
  pkgs.map(group %% _ % version).toSeq