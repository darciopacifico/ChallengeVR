package com.vr.challenge.actor.repo

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}
import akka.pattern.pipe
import com.vr.challenge.protocol.PropertyProtocol
import com.vr.challenge.protocol.PropertyProtocol._

import scala.collection._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 *
 * Created by darcio on 9/24/16.
 */
class RepoStorageActor(val initialProperties: PropertyLot, val mapProvince: Map[String, Province]) extends Actor with ActorLogging {


  //due to small amount of provinces i'll not try to optimize the search
  val listProvinces: List[(String, Province)] = this.mapProvince.toList
  val mapPropById: mutable.Map[Int, Property] = loadMapOfproperties(initialProperties)

  val idSequence = new AtomicInteger(mapPropById.keys.max)

  val setListeners = mutable.Set[ActorRef]()
  implicit val ec = context.system.dispatcher

  /**
   * Receive
   * @return
   */
  override def receive: Receive = {

    case PropertyById(id, replyTo) =>
      pipe(Future(
        PropertyByIdReply(mapPropById.get(id))
      )).to(replyTo)

    case PropertyCreate(property, replyTo) =>
      pipe(Future {

        Try {
          val newPropWithId = insertNewProperty(property)
          setListeners.foreach(_ ! NewPropertyEvent(newPropWithId))
          newPropWithId.id.get

        } match {
          case Success(id) => PropertyCreated(id)
          case Failure(err) => PropertyCreationError(err)
        }

      }).to(replyTo)

    case SubscribeForRepoEvents(listenerActor) =>
      context.watch(listenerActor)
      this.setListeners += listenerActor

    case Terminated(listenerActor) =>
      context.unwatch(listenerActor)
      this.setListeners -= listenerActor
  }

  /**
   * Load the list of properties into a map
   * @param lot
   * @return
   */
  def loadMapOfproperties(lot: PropertyLot) =
    mutable.Map[Int, Property]() ++ lot.properties.map(p => p.id.get -> p.copy(provinces = Some(getProvinces(p.lat, p.long)))).toMap


  /**
   * Get the provinces for a given lat long data
   * @param lat
   * @param long
   * @return
   */
  def getProvinces(lat: Int, long: Int): List[String] = {
    def getProvinces(listProvinces: List[(String, Province)], agg: List[String]): List[String] = {
      listProvinces match {
        case (name, province) :: ps =>
          val b = province.boundaries

          val newAgg = if (

            lat  >= b.upperLeft.x && lat <= b.bottomRight.x   &&
            long <= b.upperLeft.y && long >= b.bottomRight.y

          ) {

            name :: agg
          } else agg

          getProvinces(ps, newAgg)

        case Nil =>
          agg
      }
    }

    getProvinces(listProvinces, Nil)
  }


  /**
   * Create the new property and store locally
   * @param property
   * @return
   */
  def insertNewProperty(property: Property): PropertyProtocol.Property = {

    //proper

    val newId = idSequence.incrementAndGet()
    val provinces: List[String] = getProvinces(property.lat, property.long)
    val newProp = property.copy(id = Some(newId), provinces = Some(provinces))

    mapPropById(newId) = newProp
    newProp
  }
}
