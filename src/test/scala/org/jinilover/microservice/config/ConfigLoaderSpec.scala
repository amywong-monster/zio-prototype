package org.jinilover.microservice.config

import zio._
import zio.test.Assertion._
import zio.test._

import org.jinilover.microservice.ConfigTypes._

/**
 * Similar to [[org.jinilover.microservice.app.ConfigLoaderApp]]
 * It doesn't need to deal with the runtime IO monad execution but dedicating the task
 * to [[zio.test.DefaultRunnableSpec]]
 */
object ConfigLoaderSpec extends DefaultRunnableSpec {
  override def spec = suite("Config")(configLoaderSuite)

  private val configLoaderSuite =
    suite("Config.Loader")(testM("test `load` function") {
      val io: Task[AppConfig] = ZIO.access[Has[AppConfig]](_.get).provideLayer(ConfigLoader.live)
      val expected = AppConfig(
        DbConfig("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"),
        WebServerConfig("0.0.0.0", 8080)
      )
      assertM(io)(equalTo(expected))
    })
}
