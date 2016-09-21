package com.vr.challenge.entity

import org.json4s.JValue
import org.scalatest.{Matchers, FlatSpec}
import scala.io.Source


/**
 * Created by darcio on 9/20/16.
 */
class TestProvinceReading extends FlatSpec with Matchers {

  implicit val formats = org.json4s.DefaultFormats

  import org.json4s.jackson.JsonMethods._

  "Province file" should "be completely read" in {

    val res = getClass.getResourceAsStream("/provinces.json")
    val stream = Source.fromInputStream(res)
    val strJson = stream.getLines().mkString
    val parsed: JValue = parse(strJson)

    val mapBoundaries = parsed.extract[Map[String, Province]]

    assert(mapBoundaries.size === 6)

    mapBoundaries.keys.toSet should contain theSameElementsAs Vector("Gode", "Scavy", "Ruja", "Jaby", "Groola", "Nova")

    val provJaby = mapBoundaries("Jaby")
    assert(provJaby.boundaries.bottomRight === Corner(1400, 500))
    assert(provJaby.boundaries.upperLeft === Corner(1100, 1000))

  }
}