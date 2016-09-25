package com.vr.challenge

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import com.vr.challenge.actor.repo.RepoFacadeActor
import com.vr.challenge.actor.spray.PropertyRESTActor
import com.vr.challenge.protocol.PropertyProtocol._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import spray.can.Http

import scala.io.Source

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

    val sprayActor = system.actorOf(Props(new PropertyRESTActor(loadPropertyLot)), name = "PropertyRestAPI")

    IO(Http) ! Http.Bind(sprayActor, interface = config.getString("spray.host"), port = config.getInt("spray.port"))

  }

  /**
   * Dummy load of property lot
   * @return
   */
  def loadPropertyLot: PropertyLot = {

    val streamProperties = getClass.getResourceAsStream("/properties.json")
    val sourceProperties = Source.fromInputStream(streamProperties)
    val hugePropJson = sourceProperties.getLines().mkString
    val parsed: JValue = parse(hugePropJson)

    val propLot = parsed.extract[PropertyLot]
    propLot
  }
}
