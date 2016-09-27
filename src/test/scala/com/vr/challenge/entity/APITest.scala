package com.vr.challenge.entity

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ActorRef, Props}
import com.vr.challenge.Boot
import com.vr.challenge.actor.repo.RepoFacadeActor
import com.vr.challenge.actor.spray.APIFrontActorTrait
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.http.{StatusCodes, StatusCode, HttpEntity, MediaTypes}
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

  var strNewId: String = "666"
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
    "return another a Property By Id" in {
      Get("/properties/123") ~> routes ~> check {
        responseAs[Property].price === 347000
      }
    }
    "return 404 for an inexistent property id" in {
      Get("/properties/99999") ~> routes ~> check {
        status === NotFound
      }
    }
    "return another all 114 properties around ax=122&ay=344&bx=252&by=474" in {
      Get("/properties?ax=122&ay=474&bx=252&by=344") ~> routes ~> check {
        val propertyLot: PropertyLot = responseAs[PropertyLot]
        propertyLot.totalProperties should be greaterThan 100
        propertyLot.properties.head.price === 641000
      }
    }
    "insert a new property " in {
      Post("/properties",
        HttpEntity(MediaTypes.`application/json`,
          """
            |{ "lat": 222,
            |  "long": 444,
            |  "title": "teste criacao de imovel - post com spray test kit",
            |  "price": 1250000,
            |  "description": "desc: teste criacao de imovel - post com spray test kit",
            |  "beds": 3,
            |  "baths": 2,
            |  "squareMeters": 200}
          """.stripMargin)) ~> routes ~> check {

        strNewId = responseAs[String]
        assert(strNewId.toInt > 0)
        status === OK
        handled === true
      }
      Get(("/properties/" + strNewId)) ~> routes ~> check {
        val property: Property = responseAs[Property]
        property.price === 1250000
        property.title === "teste criacao de imovel - post com spray test kit"
        property.provinces.get === List("Scavy")
      }
    }
    "return 400 for an invalid property location " in {
      Post("/properties",
        HttpEntity(MediaTypes.`application/json`,
          """
            |{ "lat": 1420,
            |  "long": 1000,
            |  "title": "teste criacao de imovel - post com spray test kit",
            |  "price": 1250000,
            |  "description": "desc: teste criacao de imovel - post com spray test kit",
            |  "beds": 3,
            |  "baths": 2,
            |  "squareMeters": 200}
          """.stripMargin)) ~> routes ~> check {
        status === BadRequest
        handled === true
      }
    }
  }
}
