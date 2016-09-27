package com.vr.challenge.actor.spray

import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport
import spray.routing.directives.{RouteDirectives, RespondWithDirectives}
import spray.routing.{HttpService, HttpServiceActor, RequestContext}
import akka.actor.{Actor, ActorLogging, PoisonPill, _}
import spray.http.{MediaTypes, HttpEntity, HttpResponse, StatusCodes}

import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

/**
 * Route every possible reply to request context origin. Treat operation timeout
 * Created by darcio
 */
class APIFrontReplierActor(reqCtx: RequestContext, timeout: FiniteDuration) extends Actor with HttpService with RespondWithDirectives with RouteDirectives with SprayJsonSupport {
  val actorRefFactory: ActorContext = context
  implicit val ec = context.system.dispatcher

  import com.vr.challenge.protocol.PropertyProtocol._

  val timeoutSchedule = context.system.scheduler.scheduleOnce(timeout, self, RequestTimeout)

  override def receive: Receive = {
    case PropertyByGeoReply(propertyLot) =>
      reqCtx.complete(propertyLot)
      finishActor

    case PropertyByIdReply(property) =>
      reqCtx.complete(property)
      finishActor

    case PropertyCreated(id) =>
      reqCtx.complete(OK, HttpEntity(MediaTypes.`application/json`, "" + id))
      finishActor

    case PropertyCreationError(err) =>
      reqCtx.complete(HttpResponse(status = BadRequest, entity = HttpEntity(s"Error trying to create the Property! (devenv=true) ${err.getMessage()}")))
      finishActor

    case RequestTimeout =>
      reqCtx.complete(HttpResponse(status = StatusCodes.RequestTimeout, entity = HttpEntity("Request timeout!")))
      finishActor
  }

  /**
   * Cancel the timeout policy and kill the actor
   */
  private def finishActor = {
    this.timeoutSchedule.cancel()
    self ! PoisonPill
  }
}

/**
 * Companion object, containing the props definition
 */
object APIFrontReplierActor {
  def props(requestContext: RequestContext, timeout: FiniteDuration) =
    Props(new APIFrontReplierActor(requestContext, timeout))
}