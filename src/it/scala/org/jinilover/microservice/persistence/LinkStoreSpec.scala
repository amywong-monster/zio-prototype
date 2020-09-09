package org.jinilover
package microservice
package persistence

import doobie.util.transactor.Transactor

import zio.{ Has, Task, ZIO, ZLayer }
import zio.interop.catz._
import zio.clock.Clock
import zio.test._
import zio.test.Assertion._

import org.jinilover.microservice.config.ConfigLoader
import org.jinilover.microservice.ConfigTypes._
import org.jinilover.microservice.LinkTypes.{ Link, LinkStatus, UserId }
import org.jinilover.microservice.persistence.DBUtiils.createSchema

object LinkStoreSpec extends DefaultRunnableSpec {
  override def spec = suite("LinkStore")(linkStoreSuite)

  private val dbConfigIO: Task[DbConfig] =
    ZIO.access[Has[AppConfig]](_.get.db).provideLayer(ConfigLoader.live)

  private val xaIO: Task[Transactor[Task]] = dbConfigIO.map(Doobie.transactor)

  private val linkStoreSuite = suite("LinkStore.Service")(
    testM(
      "test `add` function - should add 1 link and handle uniqueKey violation or retrieve the link correctly"
    ) {
      val agda = UserId("agda")
      val idris = UserId("idris")

      val io: ZIO[Clock with Has[LinkStore.Service] with Has[Migrations.Service] with Has[
        Transactor[Task]
      ], Throwable, (Boolean, Boolean)] =
        for {
          _       <- createSchema
          store   <- ZIO.access[Has[LinkStore.Service]](_.get)
          find1   <- store.getByUniqueKey(agda, idris)
          find2   <- store.getByUniqueKey(idris, agda)
          linkExists1 = find1.isDefined || find2.isDefined

          instant <- ZIO.accessM[Clock](_.get.instant)
          _       <-
            store.add(
              Link(initiatorId = agda, targetId = idris, status = LinkStatus.Pending, creationDate = instant)
            )
          find3   <- store.getByUniqueKey(agda, idris)
          find4   <- store.getByUniqueKey(idris, agda)
          linkExists2 = find3.isDefined || find4.isDefined
        } yield (linkExists1, linkExists2)

      val result =
        io.provideLayer(Clock.live ++ LinkStore.live ++ Migrations.live ++ ZLayer.fromEffect(xaIO))
          .provideLayer(ZLayer.fromEffect(xaIO) ++ ZLayer.fromEffect(dbConfigIO))

      assertM(result)(equalTo((false, true)))
    }
  )
}
