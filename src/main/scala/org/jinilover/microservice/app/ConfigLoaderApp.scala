package org.jinilover.microservice.app

import com.github.mlangc.slf4zio.api._

import zio._

import org.jinilover.microservice.ConfigTypes._
import org.jinilover.microservice.config._

/**
 * Illustrates how to write a purely FP app on top of jvm by dedicating the runtime execution
 * of the IO monad to [[zio.App]]
 */
object ConfigLoaderApp extends App with LoggingSupport {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    ZIO
      .access[Has[AppConfig]](_.get)
      .provideLayer(ConfigLoader.live)
      .flatMap(appConfig => logger.infoIO(s"$appConfig"))
      .map(_ => ExitCode.success)
      .run
      .map {
        case Exit.Success(exitCode) => exitCode
        case Exit.Failure(_)        => ExitCode.failure
      }
}
