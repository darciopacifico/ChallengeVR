package com.vr.challenge.actor.repo

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.pattern.pipe
import akka.routing.RoundRobinPool
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
  import RepoStorageActor._

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
    mutable.Map[Int, Property]() ++ lot.properties.map(p => p.id.get -> p.copy(provinces = Some(getProvinces(listProvinces, p.lat, p.long)))).toMap

  /**
   * Busines rule for proeprty location. Spotippos only!
   * @param property
   */
  def validateLocation(property: Property) = {
    if (!(property.lat >= 0 && property.lat <= 1400) ||
        !(property.long >= 0 && property.long <= 1000))
      throw new RepoPropertyException("This property is not inside Spotippos!")
  }

  /**
   * Create the new property and store locally
   * @param property
   * @return
   */
  def insertNewProperty(property: Property): PropertyProtocol.Property = {

    validateLocation(property)

    val newId = idSequence.incrementAndGet()
    val provinces: List[String] = getProvinces(listProvinces, property.lat, property.long)
    val newProp = property.copy(id = Some(newId), provinces = Some(provinces))

    mapPropById(newId) = newProp
    newProp
  }
}

/**
 * Companion object, containing the props definition
 */

object RepoStorageActor{
  def props(lot: PropertyLot, mapProvinces: Map[String, Province]) = Props(new RepoStorageActor(lot, mapProvinces))
    .withRouter(
      RoundRobinPool(
        nrOfInstances = 1,
        supervisorStrategy =
          OneForOneStrategy(maxNrOfRetries = 10) {
            case _: ActorInitializationException => Stop
            case _: Exception => Restart
          }))



  /**
   * Get the provinces for a given lat long data
   * @param lat_x
   * @param long_y
   * @return
   */
  def getProvinces( listProvinces: List[(String, Province)], lat_x: Int, long_y: Int): List[String] = {
    def getProvinces(listProvinces: List[(String, Province)], agg: List[String]): List[String] = {
      listProvinces match {
        case (name, province) :: ps =>
          val b = province.boundaries

          val newAgg = if (
            lat_x >= b.upperLeft.x && lat_x <= b.bottomRight.x &&
              long_y >= b.bottomRight.y && long_y <= b.upperLeft.y) {

            name :: agg
          } else agg

          getProvinces(ps, newAgg)
        case Nil =>
          agg
      }
    }

    getProvinces(listProvinces, Nil)
  }
}

