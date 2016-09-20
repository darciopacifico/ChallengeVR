package com.vr

import akka.actor.Props
import akka.io.IO
import spray.can.Http

/**
 * Created by darcio on 9/20/16.
 */
object Boot {


  /**
   *
   * @param args
   */
  def main(args: Array[String]) {

    val sprayActor = system.actorOf(Props[SprayActor], name = "spray")
    IO(Http) ! Http.Bind(sprayActor, interface = config.getString("spray.host"), port = config.getInt("spray.port"))
  }

}
