package com.vr.challenge.actor.repo

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern._
import com.vr.challenge.protocol.PropertyProtocol._
import edu.wlu.cs.levy.CG.KDTree

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Future

/**
 *
 * Created by darcio on 9/24/16.
 */
class RepoGeoIndexedActor(repoActor: ActorRef, initialProperties: PropertyLot) extends Actor with ActorLogging {
  val geolocationProperties = new KDTree[mutable.Set[Property]](2)

  indexByGeolocation(initialProperties.properties: _*)

  repoActor ! SubscribeForRepoEvents(self)

  implicit val ec = context.system.dispatcher

  /**
   * Receive requests for geolocation queries and to geo index new properties
   * @return
   */
  override def receive: Receive = {
    case PropertyByGeo(ax, ay, bx, by, replyTo) =>
      pipe(Future {

        val flattenResults = geolocationProperties.range(Array(ax, ay), Array(bx, by)).asScala.toList.flatMap(p => p)

        PropertyByGeoReply(PropertyLot(flattenResults.size, flattenResults))

      }).to(replyTo)

    case NewPropertyEvent(property) =>
      indexByGeolocation(property)

  }

  /**
   * Index the property by its Geolocation
   * This operation could not be called in parallel
   * @param properties
   */
  def indexByGeolocation(properties: Property*) =
    properties.foreach { property =>
      val coords = Array(property.lat.toDouble, property.long.toDouble)
      val setProperties = getSetOfProperties(coords)
      setProperties += property
    }

  /**
   * Get the set of properties for a given latlong coordinates
   * @param coords
   * @return
   */
  def getSetOfProperties(coords: Array[Double]) =
    this.geolocationProperties.search(coords) match {
      case null =>
        val newPropSet = mutable.Set[Property]()
        this.geolocationProperties.insert(coords, newPropSet)
        newPropSet
      case propSet: mutable.Set[Property] =>
        propSet
    }

}


