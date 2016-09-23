package com.vr.challenge.actor.spray

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.util.Timeout
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.duration._
import scala.language.postfixOps

class RestInterface(repoAct: ActorRef) extends HttpServiceActor with RestApi {
  val repoActor = repoAct
  val actorContext = context

  def receive = runRoute(routes)
}

/**
 * Spray http actor to handle requests
 */
trait RestApi extends HttpService {
  //actor: Actor =>
  val actorContext: ActorRefFactory
  val repoActor: ActorRef

  val DEFAULT_REQUEST_TIMEOUT: FiniteDuration = FiniteDuration(10, TimeUnit.SECONDS)

  import com.vr.challenge.protocol.PropertyProtocol._

  implicit val timeout = Timeout(10 seconds)

  def routes: Route = sealRoute(
    pathPrefix("properties") {
      post {
        entity(as[Property]) { property => requestContext =>
          repoActor ! PropertyCreate(property, replyToZuba(requestContext))
        }
      } ~
        path(Segment) { id => requestContext =>
          repoActor ! PropertyById(id, replyToZuba(requestContext))
        } ~
        parameters('ax.as[Int], 'ay.as[Int], 'bx.as[Int], 'by.as[Int]) { (ax, ay, bx, by) =>
          get { ctx =>
            repoActor ! PropertyByGeo(ax, ay, bx, by, replyToZuba(ctx))
          }
        }
    })


  /**
   * Create a dedicated actor to handle the reply to the request context using a default timeout
   * @param requestContext
   * @return
   */
  def replyToZuba(requestContext: RequestContext): ActorRef = replyToResponder(requestContext, DEFAULT_REQUEST_TIMEOUT)

  /**
   * Create dedicated actor to handle the reply to the request context using a given timeout
   * @param requestContext
   * @param timeout
   * @return
   */
  def replyToResponder(requestContext: RequestContext, timeout: FiniteDuration): ActorRef = actorContext.actorOf(Props(new APIFrontReplierActor(requestContext, timeout)))
}
