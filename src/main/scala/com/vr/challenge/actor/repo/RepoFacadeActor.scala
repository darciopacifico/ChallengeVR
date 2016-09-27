package com.vr.challenge.actor.repo

import akka.actor._
import com.vr.challenge.protocol.PropertyProtocol._

import scala.collection.Map

/**
 * Facade point for Property repository and indexes
 * Created by darcio on 9/20/16.
 */
class RepoFacadeActor(val propertyLot: PropertyLot, val mapProvinces: Map[String, Province]) extends RepoFacadeSupervisor {

  val storageActor = createStorageActor(propertyLot, mapProvinces)
  val geoIndexedActor = createGeoIndexedActor(storageActor, propertyLot, mapProvinces)

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

/**
 * Companion object, containing the props definition
 */
object RepoFacadeActor {
  def props(lot: PropertyLot, mapProvinces: Map[String, Province]) = Props(new RepoFacadeActor(lot, mapProvinces))
}