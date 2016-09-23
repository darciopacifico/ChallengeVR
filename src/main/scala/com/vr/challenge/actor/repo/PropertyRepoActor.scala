package com.vr.challenge.actor.repo

import akka.actor.{Actor, ActorLogging}
import com.vr.challenge.protocol.PropertyProtocol._

/**
 * Created by darcio on 9/20/16.
 */
class PropertyRepoActor(val propertyLot: PropertyLot) extends Actor with ActorLogging {

  /**
   * Receive
   * @return
   */
  override def receive: Receive = {

    case PropertyById(id, replyTo) =>
      replyTo ! PropertyByIdReply(Some(getDemoProperty))

    case PropertyByGeo(xa, ya, xb, yb, replyTo) =>
      replyTo ! PropertyByGeoReply(PropertyLot(1, List(getDemoProperty)))

    case PropertyCreate(property, replyTo) =>
      replyTo ! PropertyCreated
  }

  def getDemoProperty: Property = {
    Property(Some(1), "titulo da propriedade", 100, "desc da propriedade", 10, 10, 2, 2, 200.00, Some(List("Scaly")))
  }
}
