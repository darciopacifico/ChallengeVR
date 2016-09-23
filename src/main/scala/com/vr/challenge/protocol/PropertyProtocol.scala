package com.vr.challenge.protocol

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.ActorRef
import org.json4s._
import org.json4s.ext.JavaTypesSerializers
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import spray.http.{HttpEntity, HttpCharsets}
import spray.httpx.Json4sJacksonSupport

/**
 * Basic protocol messages for actor layer
 * Created by darcio
 */

object PropertyProtocol {

  import spray.json._

  case class PropertyCreate(prop: Property, replyTo: ActorRef)

  case class PropertyCreationError(err: Exception)

  case object PropertyCreated

  case class PropertyById(id: String, replyTo: ActorRef)

  case class PropertyByIdReply(optProp: Option[Property])

  case class PropertyByGeo(ax: Int, ay: Int, bx: Int, by: Int, replyTo: ActorRef)

  case class PropertyByGeoReply(propertyLot: PropertyLot)

  case object RequestTimeout


  case class Province(boundaries: Boundaries)

  case class Corner(x: BigDecimal, y: BigDecimal)

  case class Boundaries(upperLeft: Corner, bottomRight: Corner)

  case class Property(id: Option[Int],
                      title: String,
                      price: BigDecimal,
                      description: String,
                      lat: BigDecimal,
                      long: BigDecimal,
                      beds: Int,
                      baths: Int,
                      squareMeters: Double,
                      provinces: Option[List[String]]
                     )

  case class PropertyLot(totalProperties: Int,
                         properties: List[Property])

  object Property extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[Property] = jsonFormat10(Property.apply)
  }

  object PropertyLot extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(PropertyLot.apply)
  }

  object TestProtocol2 extends DefaultJsonProtocol


  object CustomerConversions {

    implicit val liftJsonFormats = new Formats {
      val dateFormat = new DateFormat {
        val sdf = new SimpleDateFormat("yyyy-MM-dd")

        def parse(s: String): Option[Date] = try {
          Some(sdf.parse(s))
        } catch {
          case e: Exception => None
        }

        def format(d: Date): String = sdf.format(d)
      }
    }

  }

  implicit val formarttt = DefaultFormats

  implicit def HttpEntityToProperty(httpEntity: HttpEntity): Property = Serialization.read[Property](httpEntity.asString(HttpCharsets.`UTF-8`))

  implicit def HttpEntityToPropertyLot(httpEntity: HttpEntity): PropertyLot = Serialization.read[PropertyLot](httpEntity.asString(HttpCharsets.`UTF-8`))


}
