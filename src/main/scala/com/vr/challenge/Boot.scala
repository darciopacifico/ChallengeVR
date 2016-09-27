package com.vr.challenge

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import com.vr.challenge.actor.spray.APIFrontActor
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

    val sprayActor = system.actorOf(APIFrontActor.props(loadPropertyLot, loadMapProvinces), name = "APIFrontActor")
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


  def loadMapProvinces: Map[String, Province] = {

    val res = getClass.getResourceAsStream("/provinces.json")
    val stream = Source.fromInputStream(res)
    val strJson = stream.getLines().mkString
    val parsed: JValue = parse(strJson)

    val mapProvinces: Map[String, Province] = parsed.extract[Map[String, Province]]

    mapProvinces
  }


}
