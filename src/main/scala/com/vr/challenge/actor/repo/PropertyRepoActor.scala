package com.vr.challenge.actor.repo

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor}
import com.vr.challenge.actor.{PropertyCreate, PropertyByGeo, PropertyById}

/**
 * Created by darcio on 9/20/16.
 */
class PropertyRepoActor extends Actor with ActorLogging {


  override def receive: Receive = {

    case PropertyById(id, replyTo) =>

    case PropertyByGeo(xa, ya, xb, yb, replyTo) =>

    case PropertyCreate(property, replyTp) =>

  }
}
