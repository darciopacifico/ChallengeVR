package com.vr.challenge.actor.repo

import akka.actor._
import com.vr.challenge.protocol.PropertyProtocol._

/**
 * Facade point for Property repository and indexes
 * Created by darcio on 9/20/16.
 */
class RepoFacadeActor(val propertyLot: PropertyLot) extends RepoFacadeSupervisor {

  val storageActor = createStorageActor(propertyLot)
  val geoIndexedActor = createGeoIndexedActor(storageActor, propertyLot)

  /**
   * Receive and route specialized actor
   * @return
   */
  override def receive: Receive = {

    case msg: PropertyCreate =>
      storageActor ! msg

    case msg: PropertyById =>
      storageActor ! msg

    case msg: PropertyByGeo =>
      geoIndexedActor ! msg
  }

}
