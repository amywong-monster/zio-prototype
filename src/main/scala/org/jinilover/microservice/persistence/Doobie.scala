package org.jinilover
package microservice
package persistence

import java.sql.Timestamp
import java.time.Instant

import scala.reflect.runtime.universe.TypeTag

import doobie._
import doobie.implicits.javasql._

import zio.Task
import zio.interop.catz._

import ConfigTypes.DbConfig
import LinkTypes.{ LinkStatus, toLinkStatus }

object Doobie {
  implicit val LinkStatusMeta: Meta[LinkStatus] =
    Meta[String].timap(toLinkStatus)(_.toString)

  implicit val InstantMeta: Meta[Instant] =
    Meta[Timestamp].timap(_.toInstant)(Timestamp.from)

  def transactor(dbConfig: DbConfig): Transactor[Task] =
    Transactor
      .fromDriverManager[Task]("org.postgresql.Driver", dbConfig.url, dbConfig.user, dbConfig.password)
}
