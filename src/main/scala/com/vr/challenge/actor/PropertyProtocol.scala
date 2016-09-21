package com.vr.challenge.actor

import akka.actor.ActorRef
import com.vr.challenge.entity.{PropertyLot, Property}

/**
  * Basic protocol messages for actor layer
  * Created by darcio
  */

case class PropertyCreate(prop: Property, replyTo: ActorRef)
case class PropertyCreationError(err:Exception)
case object PropertyCreated

case class PropertyById(id:String, replyTo: ActorRef)
case class PropertyByIdReply(optProp:Option[Property])

case class PropertyByGeo(ax: Int, ay: Int, bx: Int, by: Int, replyTo: ActorRef)
case class PropertyByGeoReply(propertyLot: PropertyLot)

case object RequestTimeout