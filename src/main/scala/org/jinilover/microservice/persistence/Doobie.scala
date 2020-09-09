package org.jinilover
package microservice
package persistence

import java.sql.Timestamp
import java.time.Instant

import scala.reflect.runtime.universe.TypeTag

import scalaz.{ @@, Tag }

import doobie._
import doobie.implicits.javasql._

import zio._
import zio.interop.catz._

import ConfigTypes.DbConfig
import LinkTypes.{ LinkId, LinkStatus, UserId, toLinkStatus }

object Doobie {
  private def taggedMeta[A: Meta: TypeTag, T: TypeTag]: Meta[A @@ T] =
    Meta[A].timap(Tag.apply[A, T])(_.unwrap)

  implicit val LinkStatusMeta: Meta[LinkStatus] =
    Meta[String].timap(toLinkStatus)(_.toString)

  implicit val InstantMeta: Meta[Instant] =
    Meta[Timestamp].timap(_.toInstant)(Timestamp.from)

  implicit val LinkIdMeta: Meta[LinkId] =
    taggedMeta[String, LinkId.Marker]

  implicit val UserIdMeta: Meta[UserId] =
    taggedMeta[String, UserId.Marker]

  def transactor_(dbConfig: DbConfig): Transactor[Task] =
    Transactor
      .fromDriverManager[Task]("org.postgresql.Driver", dbConfig.url, dbConfig.user, dbConfig.password)
}
