package org.jinilover
package microservice
package persistence

import java.time.Instant
import java.util.UUID

import doobie.Fragments.whereAndOpt
import doobie.Transactor
import doobie.syntax.connectionio._
import doobie.syntax.string._

import zio.{ Has, Task, ZLayer }
import zio.interop.catz._

import org.jinilover.microservice.LinkTypes.{ Link, LinkStatus, SearchLinkCriteria, linkKey }
import org.jinilover.microservice.persistence.Doobie._

object LinkStore {

  trait Service {
    def add(link: Link): Task[String]

    def update(linkId: String, confirmDate: Instant, status: LinkStatus): Task[Int]

    def get(id: String): Task[Option[Link]]

    def getByUniqueKey(uid1: String, uid2: String): Task[Option[Link]]

    def getLinks(srchCriteria: SearchLinkCriteria): Task[List[String]]

    def remove(id: String): Task[Int]
  }

  val live: ZLayer[Has[Transactor[Task]], Throwable, Has[LinkStore.Service]] =
    ZLayer.fromService[Transactor[Task], Service] { xa =>
      new Service {
        override def add(link: Link): Task[String] =
          for {
            linkId <- Task.effect(UUID.randomUUID.toString)
            Link(_, initiatorId, targetId, status, creationDate, _, _) = link
            uniqueKey = linkKey(initiatorId, targetId)
            _      <- sql"""INSERT INTO links (id, initiator_id, target_id, status, creation_date, unique_key)
                            VALUES ($linkId, $initiatorId, $targetId, $status, $creationDate, $uniqueKey)
                         """.update.run.transact(xa)
          } yield linkId

        override def get(id: String): Task[Option[Link]] =
          sql"""
                SELECT id, initiator_id, target_id, status, creation_date, confirm_date, unique_key
                FROM links
                WHERE id = $id
            """.query[Link].option.transact(xa)

        override def getByUniqueKey(uid1: String, uid2: String): Task[Option[Link]] = {
          val uniqueKey = linkKey(uid1, uid2)
          sql"""
              SELECT id, initiator_id, target_id, status, creation_date, confirm_date, unique_key
              FROM links
              WHERE unique_key = $uniqueKey
            """.query[Link].option.transact(xa)
        }

        override def getLinks(srchCriteria: SearchLinkCriteria): Task[List[String]] = {
          val SearchLinkCriteria(userId, linkStatus, isInitiator) = srchCriteria

          lazy val userIsInitiator = fr"initiator_id = $userId"
          lazy val userIsTarget = fr"target_id = $userId"

          val byUserId = isInitiator.map { bool =>
            if (bool) userIsInitiator else userIsTarget
          }.orElse(Some(fr"(" ++ userIsInitiator ++ fr" OR " ++ userIsTarget ++ fr")"))

          val byLinkStatus = linkStatus.map(v => fr"status = $v")

          val fragment =
            fr"""
                SELECT id FROM links
              """ ++ whereAndOpt(byUserId, byLinkStatus)

          fragment.query[String].to[List].transact(xa)
        }

        override def update(linkId: String, confirmDate: Instant, status: LinkStatus): Task[Int] =
          sql"""
              UPDATE links
              set status = $status, confirm_date = $confirmDate
              where id = $linkId
            """.update.run.transact(xa)

        override def remove(id: String): Task[Int] =
          sql"""
              DELETE FROM links
              WHERE id = $id
            """.update.run.transact(xa)
      }
    }
}
