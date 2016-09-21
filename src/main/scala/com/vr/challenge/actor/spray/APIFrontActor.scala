package com.vr.challenge.actor.spray

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props}
import com.vr.challenge.actor.{PropertyByGeo, PropertyById, PropertyCreate}
import com.vr.challenge.entity.Property
import spray.routing.directives.{RespondWithDirectives, RouteDirectives}
import spray.routing.{HttpService, RequestContext}

import scala.concurrent.duration.FiniteDuration


/**
 * Spray http actor to handle requests
 */
class APIFrontActor(repoActor: ActorRef) extends Actor with HttpService with RespondWithDirectives with RouteDirectives {
  val DEFAULT_REQUEST_TIMEOUT: FiniteDuration = FiniteDuration(10, TimeUnit.SECONDS)

  /**
   * Routes for Properties API (POST,GET,GeoSearch)
   */
  val receive = runRoute {
    pathPrefix("properties") {
      pathEnd {
        post {
          entity(as[Property]) { property => requestContext =>
            //property creation
            repoActor ! PropertyCreate(property, replyTo(requestContext))
          }
        }
      }
      path(Segment) { id => requestContext =>
        //get property by id
        repoActor ! PropertyById(id, replyTo(requestContext))
      }
      get { requestContext =>
        parameters('ax, 'ay, 'bx, 'by) { (ax, ay, bx, by) =>
          //get property by geo coordinates
          repoActor ! PropertyByGeo(ax, ay, bx, by, replyTo(requestContext))
        }
      }
    }
  }


  /**
   * Create a dedicated actor to handle the reply to the request context using a default timeout
   * @param requestContext
   * @return
   */
  def replyTo(requestContext: RequestContext) = replyTo(requestContext, DEFAULT_REQUEST_TIMEOUT)

  /**
   * Create dedicated actor to handle the reply to the request context using a given timeout
   * @param requestContext
   * @param timeout
   * @return
   */
  def replyTo(requestContext: RequestContext, timeout: FiniteDuration) = context.actorOf(Props(new APIFrontReplierActor(requestContext, timeout)))
}
