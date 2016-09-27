package com.vr.challenge.actor.repo

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.routing.RoundRobinPool
import com.vr.challenge.protocol.PropertyProtocol.{Province, PropertyLot}

import scala.collection.Map

/**
 * Define the policies for supervision, routing and persistence of actors
 *
 * Created by darcio on 9/24/16.
 */
trait RepoFacadeSupervisor extends Actor with ActorLogging {

  def createStorageActor(lot: PropertyLot, mapProvinces: Map[String, Province]) =
    context.actorOf(RepoStorageActor.props(lot, mapProvinces), name = "RepoStorageActor")


  def createGeoIndexedActor(ref: ActorRef, lot: PropertyLot, mapProvinces: Map[String, Province]): ActorRef = {
    context.actorOf(RepoGeoIndexedActor.props(ref, lot, mapProvinces), name = "RepoGeoIndexedActor")
  }

}
