package com.vr.challenge.protocol

import akka.actor.ActorRef
import org.json4s._

/**
 * Basic protocol messages for actor layer
 * Created by darcio
 */

object PropertyProtocol {

  import spray.json._


  case class PropertyCreate(prop: Property, replyTo: ActorRef)

  case class PropertyCreationError(err: Throwable)

  case class PropertyCreated(id: Int)

  case class PropertyById(id: Int, replyTo: ActorRef)

  case class PropertyByIdReply(optProp: Option[Property])

  case class PropertyByGeo(ax: Int, ay: Int, bx: Int, by: Int, replyTo: ActorRef){
    require(ay>=by,"Invalid coordinate definition!")
    require(bx>=ax,"Invalid coordinate definition!")
  }

  case class PropertyByGeoReply(propertyLot: PropertyLot)

  case object RequestTimeout

  case class SubscribeForRepoEvents(listenerActor: ActorRef)

  case class NewPropertyEvent(property: Property)

  case class Province(boundaries: Boundaries)

  case class Boundaries(upperLeft: Corner, bottomRight: Corner)

  case class Corner(x: Int, y: Int)


  case class Property(id: Option[Int],
                      title: String,
                      price: BigDecimal,
                      description: String,
                      lat: Int,  //x
                      long: Int, //y
                      beds: Int,
                      baths: Int,
                      squareMeters: Double,
                      provinces: Option[List[String]]) {

    require(beds >= 1 && beds <= 5, "The property should have between 1 and 5 beds")
    require(baths >= 1 && baths <= 4, "The property should have between 1 and 5 baths")
    require(squareMeters >= 20 && squareMeters <= 240, "The property should have between 20 and 240 square meters")

    //require(lat >= 0 && lat <= 1400, "Lat is out of boundaries (0 to 1400)")
    //require(long >= 0 && long <= 1000, "Long is out of boundaries (0 to 1000)")

  }

  case class PropertyLot(totalProperties: Int,
                         properties: List[Property])

  object Property extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[Property] = jsonFormat10(Property.apply)
  }

  object PropertyLot extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(PropertyLot.apply)
  }

  object TestProtocol2 extends DefaultJsonProtocol

  implicit val formarttt = DefaultFormats

}
