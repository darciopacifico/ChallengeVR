package com.vr.challenge.actor

import akka.actor.{Actor, ActorContext}
import org.slf4j.LoggerFactory
import spray.routing.HttpService
import spray.routing.directives.{RespondWithDirectives, RouteDirectives}

import scala.concurrent.ExecutionContext


/**
 * Implement the routes of middleware
 * Created by Thiago Pereira on 7/21/15.
 */
class ControllerActor extends Actor with HttpService with RespondWithDirectives with RouteDirectives {

  var log = LoggerFactory.getLogger(classOf[ControllerActor])
  val actorRefFactory: ActorContext = context

  implicit def eCtx: ExecutionContext = actorRefFactory.dispatcher

  /**
   * Routes for catalog BIRO services
   */
  val receive = runRoute {
    get {
      //set of routes that retrieve data from Angus by rest
      path("item" / IntNumber) { id => complete(s"item requerido $id") } ~
        path("item" / "offer" / IntNumber) { id => complete(s"offer requerida $id") }
    } ~
      path("properties") {
        get {
          parameters('ax, 'ay, 'bx, 'by) { (ax, ay, bx, by) =>
            complete(s"Req: $ax, $ay, $bx, $by")
          }
        }
      }
  }
}