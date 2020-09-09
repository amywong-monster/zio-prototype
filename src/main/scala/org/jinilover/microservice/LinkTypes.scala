package org.jinilover
package microservice

import java.time.Instant

import scalaz.{ @@, Tag }

object LinkTypes {
  type UserId = String @@ UserId.Marker
  object UserId extends Tagger[String]

  type LinkId = String @@ LinkId.Marker
  object LinkId extends Tagger[String]

  //TODO add generic taggedtype circe codec for subsequent microservice reqt

  sealed trait LinkStatus
  object LinkStatus {
    case object Pending extends LinkStatus
    case object Accepted extends LinkStatus

    //TODO add `LinkStatus` circe codec for subsequent microservice reqt
  }

  case class Link(
    id: Option[LinkId] = None,
    initiatorId: UserId,
    targetId: UserId,
    status: LinkStatus,
    creationDate: Instant,
    confirmDate: Option[Instant] = None,
    uniqueKey: Option[String] = None
  )

  object Link {
    //TODO add `Link` circe codec for subsequent microservice reqt
  }

  case class SearchLinkCriteria(
    userId: UserId,
    linkStatus: Option[LinkStatus] = None,
    isInitiator: Option[Boolean] = None
  )

  def linkKey(userIds: UserId*): String =
    userIds.map(_.unwrap).sorted.mkString("_")

  def toLinkStatus(s: String): LinkStatus =
    if (s.toUpperCase == LinkStatus.Pending.toString.toUpperCase)
      LinkStatus.Pending
    else
      LinkStatus.Accepted

}
