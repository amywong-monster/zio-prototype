package org.jinilover
package microservice

import java.time.Instant

object LinkTypes {
  sealed trait LinkStatus
  object LinkStatus {
    case object Pending extends LinkStatus
    case object Accepted extends LinkStatus

    //TODO add `LinkStatus` circe codec for subsequent microservice reqt
  }

  case class Link(
    id: Option[String] = None,
    initiatorId: String,
    targetId: String,
    status: LinkStatus,
    creationDate: Instant,
    confirmDate: Option[Instant] = None,
    uniqueKey: Option[String] = None
  )

  object Link {
    //TODO add `Link` circe codec for subsequent microservice reqt
  }

  case class SearchLinkCriteria(
    userId: String,
    linkStatus: Option[LinkStatus] = None,
    isInitiator: Option[Boolean] = None
  )

  def linkKey(userIds: String*): String =
    userIds.sorted.mkString("_")

  def toLinkStatus(s: String): LinkStatus =
    if (s.toUpperCase == LinkStatus.Pending.toString.toUpperCase)
      LinkStatus.Pending
    else
      LinkStatus.Accepted

}
