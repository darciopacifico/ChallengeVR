package com.vr.challenge.actor.spray

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.util.Timeout
import com.vr.challenge.actor.repo.RepoFacadeActor
import com.vr.challenge.protocol.PropertyProtocol._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * (Not Spray-test-kit testable) Http actor implementation
 * @param lot
 */
class APIFrontActor(lot: PropertyLot, mapProvinces: Map[String, Province]) extends HttpServiceActor with APIFrontActorTrait {
  val repoFacadeActor = context.actorOf(RepoFacadeActor.props(lot, mapProvinces), name = "repoFacadeActor")
  val actorContext = context

  def receive = runRoute(routes)
}

/**
 * Testable trait containing the route for Rest API.
 * Spray http actor to handle requests.
 */
trait APIFrontActorTrait extends HttpService {
  val DEFAULT_REQUEST_TIMEOUT: FiniteDuration = FiniteDuration(3, TimeUnit.SECONDS)
  implicit val timeout = Timeout(10 seconds)

  val actorContext: ActorRefFactory
  val repoFacadeActor: ActorRef


  /**
   * REST routing patterns
   */
  def routes: Route =
    pathPrefix("properties") {
      post {
        entity(as[Property]) { property => requestContext =>
          repoFacadeActor ! PropertyCreate(property, replyTo(requestContext))
        }
      } ~
        path(IntNumber) { id => requestContext =>
          repoFacadeActor ! PropertyById(id, replyTo(requestContext))
        } ~
        parameters('ax.as[Int], 'ay.as[Int], 'bx.as[Int], 'by.as[Int]) { (ax, ay, bx, by) =>
          get { ctx =>
            repoFacadeActor ! PropertyByGeo(ax, ay, bx, by, replyTo(ctx))
          }
        }
    }

  /**
   * Create a dedicated actor to handle the reply to the request context using a default timeout
   * @param requestContext
   * @return
   */
  def replyTo(requestContext: RequestContext): ActorRef =
    replyTo(requestContext, DEFAULT_REQUEST_TIMEOUT)

  /**
   * Create dedicated actor to handle the reply to the request context using a given timeout
   * @param requestContext
   * @param timeout
   * @return
   */
  def replyTo(requestContext: RequestContext, timeout: FiniteDuration): ActorRef =
    actorContext.actorOf(APIFrontReplierActor.props(requestContext, timeout))

}

object APIFrontActor {
  def props(loadPropertyLot: PropertyLot, loadMapProvinces: Map[String, Province]) =
    Props(new APIFrontActor(loadPropertyLot, loadMapProvinces))
}