package com.vr.challenge.entity

import org.json4s.JValue
import org.scalatest.{Matchers, FlatSpec}
import scala.io.Source


/**
 * Created by darcio on 9/20/16.
 */
class TestProvinceReading extends FlatSpec with Matchers {
  import com.vr.challenge.protocol.PropertyProtocol._

  import org.json4s.jackson.JsonMethods._

  "Province file" should "be completely read" in {

    val res = getClass.getResourceAsStream("/provinces.json")
    val stream = Source.fromInputStream(res)
    val strJson = stream.getLines().mkString
    val parsed: JValue = parse(strJson)

    val mapProvinces: Map[String, Province] = parsed.extract[Map[String, Province]]

    assert(mapProvinces.size === 6)

    mapProvinces.keys.toSet should contain theSameElementsAs Vector("Gode", "Scavy", "Ruja", "Jaby", "Groola", "Nova")

    val provJaby = mapProvinces("Jaby")
    assert(provJaby.boundaries.bottomRight === Corner(1400, 500))
    assert(provJaby.boundaries.upperLeft === Corner(1100, 1000))

  }
}