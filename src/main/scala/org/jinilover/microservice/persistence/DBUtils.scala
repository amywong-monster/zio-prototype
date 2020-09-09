package org.jinilover.microservice.persistence

import doobie.syntax.connectionio._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0

import zio.{ Has, Task, ZIO }
import zio.interop.catz._

object DBUtils {
  val sql =
    s"""
      DROP SCHEMA public CASCADE;
      CREATE SCHEMA public;
      GRANT ALL ON SCHEMA public TO postgres;
      GRANT ALL ON SCHEMA public TO public;
    """

  def createSchema: ZIO[Has[Migrations.Service] with Has[Transactor[Task]], Throwable, Unit] =
    for {
      xa <- ZIO.access[Has[Transactor[Task]]](_.get)
      _  <- Update0(sql, None).run.transact(xa)
      _  <- ZIO.accessM[Has[Migrations.Service]](_.get.migrate)
    } yield ()
}
