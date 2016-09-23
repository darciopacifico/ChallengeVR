package com.vr.challenge.entity

import org.json4s.JValue
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source


/**
 * Created by darcio on 9/20/16.
 */
class TestPropertyReading extends FlatSpec with Matchers {
import com.vr.challenge.protocol.PropertyProtocol._

  import org.json4s.jackson.JsonMethods._

  "Property file" should "be completely read" in {

    val streamProperties = getClass.getResourceAsStream("/properties.json")
    val sourceProperties = Source.fromInputStream(streamProperties)
    val hugePropJson = sourceProperties.getLines().mkString
    val parsed: JValue = parse(hugePropJson)

    val propLot = parsed.extract[PropertyLot]

    assert(propLot.totalProperties === propLot.properties.size)
    assert(propLot.properties.size>100)

    val prop = propLot.properties(2)

    assert(prop.id            === Some(3))
    assert(prop.title         === "Imóvel código 3, com 5 quartos e 4 banheiros.")
    assert(prop.price         === 1779000)
    assert(prop.description   === "Et labore amet deserunt Lorem in tempor laboris esse in exercitation " +
                                  "laboris nisi reprehenderit. Lorem dolor non cillum laboris voluptate aliquip.")
    assert(prop.lat           === 1051)
    assert(prop.long          === 441)
    assert(prop.beds          === 5)
    assert(prop.baths         === 4)
    assert(prop.squareMeters  === 174)

    assert(prop.provinces.isEmpty)

  }
}