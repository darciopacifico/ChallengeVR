package com.vr.challenge.entity

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ActorRef, Props}
import com.vr.challenge.actor.repo.PropertyRepoActor
import com.vr.challenge.actor.spray.RestApi
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest


import scala.concurrent.duration.{FiniteDuration, DurationInt}
import scala.io.Source

/**
 *
 * Created by darcio on 9/22/16.
 */
class APITest extends Specification with Specs2RouteTest with HttpService with RestApi {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(FiniteDuration(10,TimeUnit.SECONDS))

  import com.vr.challenge.protocol.PropertyProtocol._

  def actorRefFactory = system

  val actorContext = system

  override val repoActor: ActorRef = system.actorOf(Props(new PropertyRepoActor(loadPropertyLot)))

  val smallRoute =
    get {
      pathSingleSlash {
        complete {
          <html>
            <body>
              <h1>Say hello to
                <i>spray</i>
                !</h1>
            </body>
          </html>
        }
      } ~
        path("ping") {
          complete("PONG!")
        }
    }

  import spray.httpx.SprayJsonSupport._

  implicit val formatter = DefaultJsonFormats

  "The API" should {
    "return some item " in {
      Get() ~> smallRoute ~> check {
        responseAs[String] must contain("hello")
      }
    }
    "return a Property By Id" in {
      Get("/properties/123") ~> routes ~> check {
        responseAs[String] must contain("baths")
      }
    }
    "return another a Property By Id" in {
      Get("/properties/123") ~> routes ~> check {
        responseAs[Property].price === 100
      }
    }
    "return another all properties around some area" in {
      Get("/properties?ax=22&ay=33&bx=44&by=55") ~> routes ~> check {
        val propertyLot: PropertyLot = responseAs[PropertyLot]
        propertyLot.totalProperties===1
        propertyLot.properties(0).price === 100
      }
    }
  }


  /**
   * Dummy load of property lot
   * @return
   */
  def loadPropertyLot: PropertyLot = {

    val streamProperties = getClass.getResourceAsStream("/properties.json")
    val sourceProperties = Source.fromInputStream(streamProperties)
    val hugePropJson = sourceProperties.getLines().mkString
    val parsed: JValue = parse(hugePropJson)

    val propLot = parsed.extract[PropertyLot]
    propLot
  }
}
