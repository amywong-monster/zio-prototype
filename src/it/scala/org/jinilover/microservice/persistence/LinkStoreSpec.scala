package org.jinilover
package microservice
package persistence

import doobie.syntax.connectionio._
import doobie.util.update.Update0
import doobie.util.transactor.Transactor

import zio._
import zio.interop.catz._
import zio.clock.Clock
import zio.test._
import zio.test.Assertion._

import org.jinilover.microservice.config.ConfigLoader
import org.jinilover.microservice.ConfigTypes._
import org.jinilover.microservice.LinkTypes.{ Link, LinkStatus, UserId }

object LinkStoreSpec extends DefaultRunnableSpec {
  override def spec = suite("LinkStore")(linkStoreSuite)

  private val dbConfigIO: Task[DbConfig] =
    ZIO.access[Has[AppConfig]](_.get.db).provideLayer(ConfigLoader.live)

  private val xaIO: Task[Transactor[Task]] = dbConfigIO.map(Doobie.transactor_)

  private val storeIO: Task[LinkStore.Service] = ZIO
    .access[Has[LinkStore.Service]](_.get)
    .provideLayer(LinkStore.live)
    .provideLayer(ZLayer.fromEffect(xaIO))

  private val linkStoreSuite = suite("LinkStore.Service")(
    testM(
      "test `add` function - should add 1 link and handle uniqueKey violation or retrieve the link correctly"
    ) {
      val agda = UserId("agda")
      val idris = UserId("idris")

      val result: Task[(Boolean, Boolean)] =
        for {
          _       <- createSchema
          store   <- storeIO
          find1   <- store.getByUniqueKey(agda, idris)
          find2   <- store.getByUniqueKey(idris, agda)
          linkExists1 = find1.isDefined || find2.isDefined

          instant <- ZIO.accessM[Clock](_.get.instant).provideLayer(Clock.live)
          _       <- store.add(
                       Link(
                         initiatorId = agda,
                         targetId = idris,
                         status = LinkStatus.Pending,
                         creationDate = instant
                       )
                     )
          find3   <- store.getByUniqueKey(agda, idris)
          find4   <- store.getByUniqueKey(idris, agda)
          linkExists2 = find3.isDefined || find4.isDefined
        } yield (linkExists1, linkExists2)

      assertM(result)(equalTo((false, true)))
    }
  )

  private def createSchema: Task[Unit] = {
    val sql =
      s"""
      DROP SCHEMA public CASCADE;
      CREATE SCHEMA public;
      GRANT ALL ON SCHEMA public TO postgres;
      GRANT ALL ON SCHEMA public TO public;
    """

    val io =
      for {
        xa <- xaIO
        _  <- Update0(sql, None).run.transact(xa)
        _  <- ZIO.accessM[Has[Migrations.Service]](_.get.migrate).provideLayer(Migrations.live)
      } yield ()

    io.provideLayer(ZLayer.fromEffect(dbConfigIO))
  }
}
