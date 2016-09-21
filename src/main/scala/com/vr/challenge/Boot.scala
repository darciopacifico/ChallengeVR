package com.vr.challenge

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import com.vr.challenge.actor.spray.APIFrontActor
import spray.can.Http

/**
 * Created by darcio
 */
object Boot {
  implicit val system = ActorSystem()
  val config = ConfigFactory.load()
  /**
   *
   * @param args
   */
  def main(args: Array[String]) {

    val sprayActor = system.actorOf(Props[APIFrontActor], name = "spray")
    IO(Http) ! Http.Bind(sprayActor, interface = config.getString("spray.host"), port = config.getInt("spray.port"))
  }

}
