package org.jinilover.microservice.app

import com.github.mlangc.slf4zio.api._

import doobie.util.transactor.Transactor

import zio.{ App, Exit, ExitCode, Has, Task, TaskLayer, URIO, ZIO, ZLayer }
import zio.interop.catz._
import zio.clock.Clock

import org.jinilover.microservice.config.ConfigLoader
import org.jinilover.microservice.ConfigTypes._
import org.jinilover.microservice.LinkTypes.{ Link, LinkStatus }
import org.jinilover.microservice.persistence.DBUtils.createSchema
import org.jinilover.microservice.persistence.{ Doobie, LinkStore, Migrations }

/**
 * An application illustrates how to use [[LinkStore.Service]].
 */
object LinkStoreApp extends App with LoggingSupport {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val agda = "agda"
    val idris = "idris"

    val io: ZIO[Clock with Has[LinkStore.Service] with Has[Migrations.Service] with Has[
      Transactor[Task]
    ], Throwable, Unit] =
      for {
        _       <- createSchema
        store   <- ZIO.access[Has[LinkStore.Service]](_.get)
        find1   <- store.getByUniqueKey(agda, idris)
        find2   <- store.getByUniqueKey(idris, agda)
        anyLinkAfterDbCleanup = find1.isDefined || find2.isDefined
        _       <- if (anyLinkAfterDbCleanup)
                     logger.errorIO("um, the db should be empty at this stage")
                   else
                     logger.infoIO(s"Yes! $agda is not associated with $idris yet")

        instant <- ZIO.accessM[Clock](_.get.instant)
        _       <- store.add(
                     Link(initiatorId = agda, targetId = idris, status = LinkStatus.Pending, creationDate = instant)
                   )
        find3   <- store.getByUniqueKey(agda, idris)
        find4   <- store.getByUniqueKey(idris, agda)
        anyLinkAfterDataInsertion = find3.isDefined || find4.isDefined
        _       <- if (anyLinkAfterDataInsertion)
                     logger.infoIO(s"Yes! $agda and $idris are friends now")
                   else
                     logger.errorIO(s"Why? They are still not related")
      } yield ()

    val dbConfigLayer = ZLayer.fromEffect(ConfigLoader.io.map(_.db))

    val xaLayer = ZLayer.fromEffect(Doobie.transactor.provideLayer(dbConfigLayer))

    io
      .provideLayer(Clock.live ++ LinkStore.live ++ Migrations.live ++ xaLayer)
      .provideLayer(xaLayer ++ dbConfigLayer)
      .map(_ => ExitCode.success)
      .run
      .map {
        case Exit.Success(exitCode) => exitCode
        case Exit.Failure(_)        => ExitCode.failure
      }

  }
}
