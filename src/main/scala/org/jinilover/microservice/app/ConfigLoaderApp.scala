package org.jinilover.microservice.app

import com.github.mlangc.slf4zio.api._

import zio.{ App, Exit, ExitCode, URIO }

import org.jinilover.microservice.config.ConfigLoader

/**
 * An application illustrates how to call [[ConfigLoader]].
 * The difference from [[scala.App]] is this is purely functional by using IO monad to deal with interaction
 * with the external system (in this case is file and console).  It dedicates the IO monad execution to [[zio.App]]
 */
object ConfigLoaderApp extends App with LoggingSupport {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    ConfigLoader.io
      .flatMap(appConfig => logger.infoIO(s"$appConfig"))
      .map(_ => ExitCode.success)
      .run
      .map {
        case Exit.Success(exitCode) => exitCode
        case Exit.Failure(_)        => ExitCode.failure
      }
}
