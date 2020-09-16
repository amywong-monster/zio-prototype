import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings

name := "zio-prototype"

version := "0.1"

scalaVersion := "2.12.7"

organization := "org.jinilover"

libraryDependencies ++= Dependencies.compile ++ Dependencies.test

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

