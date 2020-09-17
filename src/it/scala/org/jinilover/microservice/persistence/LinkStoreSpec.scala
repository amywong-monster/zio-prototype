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
import org.jinilover.microservice.LinkTypes.{ Link, LinkStatus }
import org.jinilover.microservice.persistence.DBUtils.createSchema

/**
 * [[LinkStore.Service]] unit test
 */
object LinkStoreSpec extends DefaultRunnableSpec {
  override def spec = suite("LinkStore")(linkStoreSuite)

  private val dbConfigLayer = ZLayer.fromEffect(ConfigLoader.io.map(_.db))

  private val xaLayer = ZLayer.fromEffect(Doobie.transactor.provideLayer(dbConfigLayer))

  private val linkStoreSuite =
    suite("LinkStore.Service")(testM("test `add` function - should add 1 link successfully to db") {
      val agda = "agda"
      val idris = "idris"

      val io: ZIO[Clock with Has[LinkStore.Service] with Has[Migrations.Service] with Has[
        Transactor[Task]
      ], Throwable, (Boolean, Boolean)] =
        for {
          _       <- createSchema
          store   <- ZIO.access[Has[LinkStore.Service]](_.get)
          find1   <- store.getByUniqueKey(agda, idris)
          find2   <- store.getByUniqueKey(idris, agda)
          anyLinkAfterDbCleanup = find1.isDefined || find2.isDefined

          instant <- ZIO.accessM[Clock](_.get.instant)
          _       <-
            store.add(
              Link(initiatorId = agda, targetId = idris, status = LinkStatus.Pending, creationDate = instant)
            )
          find3   <- store.getByUniqueKey(agda, idris)
          find4   <- store.getByUniqueKey(idris, agda)
          anyLinkAfterDataInsertion = find3.isDefined || find4.isDefined
        } yield (anyLinkAfterDbCleanup, anyLinkAfterDataInsertion)

      val result =
        io.provideLayer(Clock.live ++ LinkStore.live ++ Migrations.live ++ xaLayer)
          .provideLayer(xaLayer ++ dbConfigLayer)

      assertM(result)(equalTo((false, true)))
    })
}
