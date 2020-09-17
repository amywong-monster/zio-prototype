package org.jinilover
package microservice
package config

import scala.util.Try

import pureconfig._
import pureconfig.generic.auto._

import zio._

import org.jinilover.microservice.ConfigTypes.AppConfig

object ConfigLoader {
  val io: Task[AppConfig] = ZIO.fromTry(Try(ConfigSource.default.loadOrThrow[AppConfig]))
  val live: Layer[Throwable, Has[AppConfig]] = ZLayer.fromEffect(io)
}
