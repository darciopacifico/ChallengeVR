package com.vr.challenge.actor.repo

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.routing.RoundRobinPool
import com.vr.challenge.protocol.PropertyProtocol.PropertyLot

/**
 * Define the policies for supervision, routing and persistence of actors
 *
 * Created by darcio on 9/24/16.
 */
trait RepoFacadeSupervisor extends Actor with ActorLogging {

  def createStorageActor(lot: PropertyLot): ActorRef = {
    context.actorOf(Props(new RepoStorageActor(lot))
      .withRouter(
        RoundRobinPool(
          nrOfInstances = 1,
          supervisorStrategy =
            OneForOneStrategy(maxNrOfRetries = 10) {
              case _: ActorInitializationException => Stop
              case _: Exception => Restart
            })))
  }

  def createGeoIndexedActor(ref: ActorRef, lot: PropertyLot): ActorRef = {
    context.actorOf(Props(new RepoGeoIndexedActor(ref, lot))
      .withRouter(
        RoundRobinPool(
          nrOfInstances = 6,
          supervisorStrategy =
            OneForOneStrategy(maxNrOfRetries = 10) {
              case _: ActorInitializationException => Stop
              case _: Exception => Restart
            })))
  }

}
