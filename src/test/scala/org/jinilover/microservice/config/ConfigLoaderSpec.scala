package org.jinilover.microservice.config

import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, assertM, suite, testM }

import org.jinilover.microservice.ConfigTypes._

/**
 * [[ConfigLoader]] unit test
 * Like [[org.jinilover.microservice.app.ConfigLoaderApp]],
 * it doesn't need to deal with the IO monad execution but dedicating the work
 * to [[DefaultRunnableSpec]]
 */
object ConfigLoaderSpec extends DefaultRunnableSpec {
  override def spec = suite("Config")(configLoaderSuite)

  private val configLoaderSuite =
    suite("ConfigLoader")(testM("test if it loads config information correct from application.conf") {
      val expected = AppConfig(
        DbConfig("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"),
        WebServerConfig("0.0.0.0", 8080)
      )
      assertM(ConfigLoader.io)(equalTo(expected))
    })
}
