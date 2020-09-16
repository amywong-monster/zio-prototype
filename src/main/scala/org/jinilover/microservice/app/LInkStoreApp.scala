package org.jinilover.microservice.app

import com.github.mlangc.slf4zio.api._

import doobie.util.transactor.Transactor

import zio.{ App, Exit, ExitCode, Has, Task, URIO, ZIO, ZLayer }
import zio.interop.catz._
import zio.clock.Clock

import org.jinilover.microservice.config.ConfigLoader
import org.jinilover.microservice.ConfigTypes._
import org.jinilover.microservice.LinkTypes.{ Link, LinkStatus }
import org.jinilover.microservice.persistence.DBUtils.createSchema
import org.jinilover.microservice.persistence.{ Doobie, LinkStore, Migrations }

/**
 * Similar purpose as [[org.jinilover.microservice.app.ConfigLoaderApp]]
 */
object LinkStoreApp extends App with LoggingSupport {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val haskell = "haskell"
    val ocaml = "ocaml"

    val io: ZIO[Clock with Has[LinkStore.Service] with Has[Migrations.Service] with Has[
      Transactor[Task]
    ], Throwable, Unit] =
      for {
        _       <- createSchema
        store   <- ZIO.access[Has[LinkStore.Service]](_.get)
        find1   <- store.getByUniqueKey(haskell, ocaml)
        find2   <- store.getByUniqueKey(ocaml, haskell)
        anyLinkAfterDbCleanup = find1.isDefined || find2.isDefined
        _       <- if (anyLinkAfterDbCleanup)
                     logger.errorIO("um, the db should be empty at this stage")
                   else
                     logger.infoIO(s"Yes! $haskell is not associated with $ocaml yet")

        instant <- ZIO.accessM[Clock](_.get.instant)
        _       <-
          store.add(
            Link(initiatorId = haskell, targetId = ocaml, status = LinkStatus.Pending, creationDate = instant)
          )
        find3   <- store.getByUniqueKey(haskell, ocaml)
        find4   <- store.getByUniqueKey(ocaml, haskell)
        anyLinkAfterDataInsertion = find3.isDefined || find4.isDefined
        _       <- if (anyLinkAfterDataInsertion)
                     logger.infoIO(s"Yes! Data inserted")
                   else
                     logger.errorIO(s"Why? They are still not related")
      } yield ()

    val dbConfigIO: Task[DbConfig] =
      ZIO.access[Has[AppConfig]](_.get.db).provideLayer(ConfigLoader.live)

    val xaIO: Task[Transactor[Task]] = dbConfigIO.map(Doobie.transactor)

    io
      .provideLayer(Clock.live ++ LinkStore.live ++ Migrations.live ++ ZLayer.fromEffect(xaIO))
      .provideLayer(ZLayer.fromEffect(xaIO) ++ ZLayer.fromEffect(dbConfigIO))
      .map(_ => ExitCode.success)
      .run
      .map {
        case Exit.Success(exitCode) => exitCode
        case Exit.Failure(_)        => ExitCode.failure
      }

  }
}
