package com.vr.challenge.actor.spray

import akka.actor.Actor.Receive
import akka.actor.{PoisonPill, ActorLogging, Actor}
import com.vr.challenge.actor._
import _root_.spray.http.{HttpEntity, HttpResponse}
import _root_.spray.routing.RequestContext

import scala.concurrent.duration.FiniteDuration

/**
 * Route every possible reply to request context origin. Treat operation timeout too
 * Created by darcio
 */
class APIFrontReplierActor(reqCtx: RequestContext, timeout: FiniteDuration) extends Actor with ActorLogging {

  context.system.scheduler.scheduleOnce(timeout, self, RequestTimeout);

  override def receive: Receive = {
    case PropertyByGeoReply(propertyLot) =>
      reqCtx.complete(propertyLot)
      finishReplyActor

    case PropertyByIdReply(property) =>
      reqCtx.complete(property)
      finishReplyActor

    case PropertyCreated =>
      reqCtx.complete(HttpResponse(status = 200))
      finishReplyActor

    case PropertyCreationError(err) =>
      reqCtx.complete(HttpResponse(status = 500, entity = HttpEntity(s"Error trying to create the Property! (devenv=true) ${err.getMessage()}")))
      finishReplyActor

    case RequestTimeout =>
      reqCtx.complete(HttpResponse(status = 408, entity = HttpEntity("Request timeout!")))
      finishReplyActor

    case _ =>
      reqCtx.complete(HttpResponse(status = 500, entity = HttpEntity("Not expected response!")))
      finishReplyActor

  }

  private def finishReplyActor = self ! PoisonPill
}