package org.jinilover.microservice.app

import com.github.mlangc.slf4zio.api._

import zio._

import org.jinilover.microservice.ConfigTypes._
import org.jinilover.microservice.config._

object ConfigLoaderApp extends App with LoggingSupport {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    (for {
      appConfig <- ZIO.access[Has[AppConfig]](_.get).provideLayer(ConfigLoader.live)
      _         <- logger.infoIO(s"$appConfig")
    } yield ExitCode.success).run.map {
      case Exit.Success(exitCode) => exitCode
      case Exit.Failure(_)        => ExitCode.failure
    }
  }
}
