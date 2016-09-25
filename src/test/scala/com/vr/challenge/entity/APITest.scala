package com.vr.challenge.entity

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ActorRef, Props}
import com.vr.challenge.Boot
import com.vr.challenge.actor.repo.RepoFacadeActor
import com.vr.challenge.actor.spray.APIFrontActorTrait
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
class APITest extends Specification with Specs2RouteTest with HttpService with APIFrontActorTrait {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(FiniteDuration(10, TimeUnit.SECONDS))

  import com.vr.challenge.protocol.PropertyProtocol._

  def actorRefFactory = system

  val actorContext = system

  override val repoFacadeActor: ActorRef = system.actorOf(Props(new RepoFacadeActor(Boot.loadPropertyLot, Boot.loadMapProvinces)))

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
        responseAs[Property].price === 347000
      }
    }
    "return another all 114 properties around ax=122&ay=344&bx=252&by=474" in {
      Get("/properties?ax=122&ay=344&bx=252&by=474") ~> routes ~> check {
        val propertyLot: PropertyLot = responseAs[PropertyLot]
        propertyLot.totalProperties === 114
        propertyLot.properties.head.price === 641000
      }
    }
  }


}
