package org.jinilover
package microservice
package persistence

import scala.util.Try

import com.github.mlangc.slf4zio.api._

import org.flywaydb.core.Flyway

import zio._

import org.jinilover.microservice.ConfigTypes.DbConfig

object Migrations extends LoggingSupport {
  trait Service {
    def migrate: Task[Unit]
  }

  val live: ZLayer[Has[DbConfig], Throwable, Has[Migrations.Service]] =
    ZLayer.fromService[DbConfig, Migrations.Service] { dbConfig =>
      new Service {
        override def migrate: Task[Unit] =
          ZIO.fromTry {
            Try {
              val flyway = new Flyway()
              flyway.setDataSource(dbConfig.url, dbConfig.user, dbConfig.password)
              flyway.migrate()
            }
          }.flatMap { i =>
            logger.infoIO(s"$i migrations performed")
          }
      }
    }

}
