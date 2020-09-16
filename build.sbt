import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings

name := "zio-prototype"

version := "0.1"

scalaVersion := "2.12.7"

organization := "org.jinilover"

lazy val zioV = "1.0.1"

// dependencies for compilation to application
libraryDependencies ++= Seq("org.tpolecat" %% "doobie-core", "org.tpolecat" %% "doobie-postgres").map(
  _ % "0.8.8"
)
libraryDependencies += "com.github.pureconfig" %% "pureconfig"         % "0.12.3"
libraryDependencies += "dev.zio"               %% "zio"                % zioV
libraryDependencies += "dev.zio"               %% "zio-interop-cats"   % "2.1.4.0"
libraryDependencies += "com.github.mlangc"     %% "zio-interop-log4j2" % "1.0.0-RC21"
libraryDependencies += "com.github.mlangc"     %% "slf4zio"            % "1.0.0-RC21-2"
libraryDependencies += "ch.qos.logback"         % "logback-classic"    % "1.2.3"
libraryDependencies += "org.flywaydb"           % "flyway-core"        % "5.0.7"

libraryDependencies ++= Seq(
  "dev.zio"  %% "zio-test"     % zioV,
  "dev.zio"  %% "zio-test-sbt" % zioV,
  "io.monix" %% "monix"        % "3.2.1"
).map(_ % "it, test")

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3")
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")

// required for running integration test such as it:compile, it:test
configs(IntegrationTest)
Defaults.itSettings
inConfig(IntegrationTest)(scalafmtConfigSettings)

// enable integration test to depend on the test folder sources
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

// required for running zio test
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
