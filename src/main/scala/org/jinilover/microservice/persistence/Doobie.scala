package org.jinilover
package microservice
package persistence

import java.sql.Timestamp
import java.time.Instant

import doobie._
import doobie.implicits.javasql._

import zio.{ Has, Task, URIO, ZIO }
import zio.interop.catz._

import ConfigTypes.DbConfig
import LinkTypes.{ LinkStatus, toLinkStatus }

/**
 * Maps data types to database compatible types
 */
object Doobie {
  implicit val LinkStatusMeta: Meta[LinkStatus] =
    Meta[String].timap(toLinkStatus)(_.toString)

  implicit val InstantMeta: Meta[Instant] =
    Meta[Timestamp].timap(_.toInstant)(Timestamp.from)

  def transactor: URIO[Has[DbConfig], Transactor[Task]] =
    ZIO.access[Has[DbConfig]] { hasDbConfig =>
      val dbConfig = hasDbConfig.get
      Transactor
        .fromDriverManager[Task]("org.postgresql.Driver", dbConfig.url, dbConfig.user, dbConfig.password)
    }
}
