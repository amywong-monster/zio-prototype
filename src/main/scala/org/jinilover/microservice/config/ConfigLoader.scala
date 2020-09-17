package org.jinilover
package microservice
package config

import scala.util.Try

import pureconfig._
import pureconfig.generic.auto._

import zio.{ Has, Layer, Task, ZIO, ZLayer }

import org.jinilover.microservice.ConfigTypes.AppConfig

/**
 * Load information from `application.conf` to [[AppConfig]]
 */
object ConfigLoader {
  val io: Task[AppConfig] = ZIO.fromTry(Try(ConfigSource.default.loadOrThrow[AppConfig]))

  val live: Layer[Throwable, Has[AppConfig]] = ZLayer.fromEffect(io)
}
