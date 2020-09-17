package org.jinilover
package microservice

import java.time.Instant

/**
 * Types for data store in database
 */
object LinkTypes {
  sealed trait LinkStatus
  object LinkStatus {
    case object Pending extends LinkStatus
    case object Accepted extends LinkStatus

    //TODO add `LinkStatus` json codec for subsequent REST service reqt
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
    //TODO add `Link` circe json for subsequent REST service reqt
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
