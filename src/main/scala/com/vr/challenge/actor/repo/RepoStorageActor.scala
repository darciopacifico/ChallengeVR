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
class RepoStorageActor(val initialProperties: PropertyLot) extends Actor with ActorLogging {

  val mapPropById = loadMapOfproperties(initialProperties)
  val setListeners = mutable.Set[ActorRef]()

  val incrementer = new AtomicInteger(mapPropById.keys.max)
  implicit val ec = context.system.dispatcher

  /**
   * Receive
   * @return
   */
  override def receive: Receive = {

    case PropertyById(strId, replyTo) =>
      pipe(Future {
        Try(strId.toInt) match {
          case Success(id) => PropertyByIdReply(mapPropById.get(id))
          case Failure(err) => PropertyCreationError(err)
        }

      }).to(replyTo)

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
   * Create the new property and store locally
   * @param property
   * @return
   */
  def insertNewProperty(property: Property): PropertyProtocol.Property = {
    val newId = incrementer.incrementAndGet()
    val newProp = property.copy(id = Some(newId))
    mapPropById(newId) = newProp
    newProp
  }

  /**
   * Load the list of properties into a map
   * @param lot
   * @return
   */
  def loadMapOfproperties(lot: PropertyLot) =
    mutable.Map[Int, Property]() ++ lot.properties.map(p => p.id.get -> p).toMap

}


