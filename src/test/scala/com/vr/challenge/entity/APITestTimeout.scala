package com.vr.challenge.entity

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import com.vr.challenge.Boot
import com.vr.challenge.actor.repo.RepoFacadeActor
import com.vr.challenge.actor.spray.APIFrontActorTrait
import org.json4s._
import org.specs2.mutable.Specification
import spray.http.{HttpEntity, MediaTypes, StatusCodes}
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

import scala.collection.Map
import scala.concurrent.duration.FiniteDuration

/**
 *
 * Created by darcio on 9/22/16.
 */
class APITestTimeout extends Specification with Specs2RouteTest with HttpService with APIFrontActorTrait {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(FiniteDuration(10, TimeUnit.SECONDS))

  import com.vr.challenge.protocol.PropertyProtocol._

  def actorRefFactory = system

  val actorContext = system

  override val repoFacadeActor: ActorRef = system.actorOf(Props(new LazyRepoFacadeActor(Boot.loadPropertyLot, Boot.loadMapProvinces)))

  implicit val formatter = DefaultJsonFormats

  "The API" should {
    "handle timeout for a valid get by id" in {
      Get("/properties/123") ~> routes ~> check {
        status === StatusCodes.RequestTimeout
      }
    }
    "handle timeout for an invalid get by id" in {
      Get("/properties/99999") ~> routes ~> check {
        status === StatusCodes.RequestTimeout
      }
    }
    "handle timeout for range search" in {
      Get("/properties?ax=122&ay=474&bx=252&by=344") ~> routes ~> check {
        status === StatusCodes.RequestTimeout
      }
    }
    "handle timeout for insertions " in {
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
        status === StatusCodes.RequestTimeout
      }
    }
  }

  /**
   * Lazy version of RepoFacadeActor. Call thread sleep for every message. Test purposes
   * @param propertyLot
   * @param mapProvinces
   */
  class LazyRepoFacadeActor(propertyLot: PropertyLot, mapProvinces: Map[String, Province]) extends RepoFacadeActor(propertyLot, mapProvinces) {
    /**
     * Receive and route specialized actor
     * @return
     */
    override def receive: Receive = {
      // check APIFrontActorTrait.DEFAULT_REQUEST_TIMEOUT for defaul timeout: 3s
      case msg: PropertyCreate =>
        Thread.sleep(3020)
        storageActor ! msg

      case msg: PropertyById =>
        Thread.sleep(3020)
        storageActor ! msg

      case msg: PropertyByGeo =>
        Thread.sleep(3020)
        geoIndexedActor ! msg
    }
  }

}
