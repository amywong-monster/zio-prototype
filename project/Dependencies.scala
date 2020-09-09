import sbt._

object Dependencies {
  val zioV = "1.0.1"

  lazy val compile = Seq(
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

  lazy val test = Seq(
    testDep("dev.zio", zioV, "zio-test", "zio-test-sbt"),
    testDep("io.monix", "3.2.1", "monix")
  ).flatten

  def testDep(group: String, version: String, pkgs: String*) =
    dep(group, version, pkgs: _*).map(_ % "it, test").toSeq

  def dep(group: String, version: String, pkgs: String*) =
    pkgs.map(group %% _ % version).toSeq
}
