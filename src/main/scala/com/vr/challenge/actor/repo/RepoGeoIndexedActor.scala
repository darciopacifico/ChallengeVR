package com.vr.challenge.actor.repo

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.pattern._
import akka.routing.RoundRobinPool
import com.vr.challenge.protocol.PropertyProtocol._
import edu.wlu.cs.levy.CG.KDTree

import scala.collection.JavaConverters._
import scala.collection.{Map, mutable}
import scala.concurrent.Future

/**
 *
 * Created by darcio on 9/24/16.
 */
class RepoGeoIndexedActor(repoActor: ActorRef, initialProperties: PropertyLot, mapProvince: Map[String, Province]) extends Actor with ActorLogging {
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

        val flattenResults = geolocationProperties.range(Array(ax, by), Array(bx, ay)).asScala.toList.flatMap(p => p)
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
  def indexByGeolocation(properties: Property*) = {
    val listProvinces: List[(String, Province)] = this.mapProvince.toList
    properties.foreach { property =>
      val provinces = RepoStorageActor.getProvinces(listProvinces, property.lat, property.long)
      val coords = Array(property.lat.toDouble, property.long.toDouble)
      val setProperties = getSetOfProperties(coords)
      setProperties += property.copy(provinces = Some(provinces))
    }
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


/**
 * Companion object, containing the props definition
 */
object RepoGeoIndexedActor {
  def props(ref: ActorRef, lot: PropertyLot, mapProvince: Map[String, Province]) = Props(new RepoGeoIndexedActor(ref, lot, mapProvince))
    .withRouter(
      RoundRobinPool(
        nrOfInstances = 6,
        supervisorStrategy =
          OneForOneStrategy(maxNrOfRetries = 10) {
            case _: ActorInitializationException => Stop
            case _: Exception => Restart
          }))
}