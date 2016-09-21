package com.vr.challenge

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import com.vr.challenge.actor.ControllerActor
import spray.can.Http

/**
 * Created by darcio on 9/20/16.
 */
object Boot {
  implicit val system = ActorSystem()
  val config = ConfigFactory.load()
  /**
   *
   * @param args
   */
  def main(args: Array[String]) {

    val sprayActor = system.actorOf(Props[ControllerActor], name = "spray")
    IO(Http) ! Http.Bind(sprayActor, interface = config.getString("spray.host"), port = config.getInt("spray.port"))
  }

}
