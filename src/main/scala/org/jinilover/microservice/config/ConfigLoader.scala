package org.jinilover
package microservice
package config

import scala.util.Try

import pureconfig._
import pureconfig.generic.auto._

import zio._

import org.jinilover.microservice.ConfigTypes.AppConfig

object ConfigLoader {
  val live: Layer[Throwable, Has[AppConfig]] =
    ZLayer.fromEffect {
      ZIO.fromTry {
        Try(ConfigSource.default.at("org.jinilover.microservice").loadOrThrow[AppConfig])
      }
    }
}
