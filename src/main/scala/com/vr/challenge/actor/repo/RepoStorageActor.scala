package com.vr.challenge.actor.repo

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}
import akka.pattern.pipe
import akka.routing.{Routees, GetRoutees, RouterRoutees}
import com.vr.challenge.protocol.PropertyProtocol
import com.vr.challenge.protocol.PropertyProtocol._

import scala.collection._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 *
 * Created by darcio on 9/24/16.
 */
class RepoStorageActor(val initialProperties: PropertyLot, val mapProvinces: Map[String, Province]) extends Actor with ActorLogging {

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
          setListeners.foreach(g =>
            g ! NewPropertyEvent(newPropWithId)
          )
        } match {
          case Success(_) => PropertyCreated
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
    mutable.Map[Int, Property]() ++ lot.properties.map(p => p.id.get -> p).toMap

  /**
   *
   * @param property
   */
  def validateProperty(property: Property) = {
    //0 <= x <= 1400 e 0 <= y <= 1000

    if (property.lat < 0 || property.lat > 1400 || property.long < 0 || property.long > 1000)
      throw new RepoPropertyException("Invalid lat long properties definitions!")

  }

  /**
   * Create the new property and store locally
   * @param property
   * @return
   */
  def insertNewProperty(property: Property): PropertyProtocol.Property = {

    validateProperty(property)

    val newId = idSequence.incrementAndGet()
    val newProp = property.copy(id = Some(newId))
    mapPropById(newId) = newProp
    newProp
  }
}
