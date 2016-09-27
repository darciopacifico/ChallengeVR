package com.vr.challenge.actor.repo

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.vr.challenge.Boot
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


/**
 *
 * Created by darcio on 9/25/16.
 */
class RepoFacadeActorTest extends TestKit(ActorSystem("TestKitUsageSpec")) with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  import com.vr.challenge.protocol.PropertyProtocol._

  val lot = Boot.loadPropertyLot
  val mapProvinces = Boot.loadMapProvinces
  val repoFacadeActor = system.actorOf(Props(new RepoFacadeActor(lot, mapProvinces)))

  var newPropId: Int = _

  "The RepoFacadeActor" should {
    "return an existing property by its ID" in {
      repoFacadeActor ! PropertyById(234, self)
      val p: Property = expectMsgClass(classOf[PropertyByIdReply]).optProp.get
      p.id should be(Some(234))
      p.beds should be(3)
      assert(p.provinces.isDefined)
      p.provinces.get should contain only "Scavy"
    }

    "return the property id 14, containing its 2 provinces" in {
      repoFacadeActor ! PropertyById(14, self)
      val p = expectMsgClass(classOf[PropertyByIdReply]).optProp.get
      p.id should be(Some(14))
      assert(p.provinces.isDefined)
      p.provinces.get should contain only("Ruja", "Gode")
    }

    "find no property for id=99999 " in {
      repoFacadeActor ! PropertyById(99999, self)
      val reply = expectMsgClass(classOf[PropertyByIdReply])
      assert(reply.optProp.isEmpty)
    }

    "find nothing in the region 199, 301, 201, 299 " in {
      repoFacadeActor ! PropertyByGeo(199, 301, 201, 299, self)
      val reply = expectMsgClass(classOf[PropertyByGeoReply])
      assert(reply.propertyLot.totalProperties == 0)
      assert(reply.propertyLot.properties.isEmpty)
    }

    "insert a new Property into the region 200,300,200,300 " in {
      repoFacadeActor ! PropertyCreate(getNewProperty(), self)
      val PropertyCreated(newPropId) = expectMsgClass(classOf[PropertyCreated])
      this.newPropId = newPropId
    }

    "find the recently created property by its id" in {
      repoFacadeActor ! PropertyById(this.newPropId, self)
      val PropertyByIdReply(Some(theNewProp)) = expectMsgClass(classOf[PropertyByIdReply])
      theNewProp.title should be("nova casa teste")
      theNewProp.provinces.get should contain only "Scavy"
    }

    "find the new inserted Property in the region 199, 301, 201, 299 " in {
      repoFacadeActor ! PropertyByGeo(199, 301, 201, 299, self)
      val reply = expectMsgClass(classOf[PropertyByGeoReply])
      val newProp: Property = reply.propertyLot.properties.head

      newProp.title should be("nova casa teste")
      newProp.provinces.get should contain only "Scavy"
      newProp.squareMeters should be(123)
    }

  }


  def getNewProperty(): Property = Property(
    id = None,
    title = "nova casa teste",
    price = 234234,
    description = "nova casa teste desc",
    lat = 200,
    long = 300,
    beds = 3,
    baths = 3,
    squareMeters = 123,
    provinces = None
  )

}
