package com.vr.challenge.entity

import com.vr.challenge.protocol.PropertyProtocol.Property
import org.scalatest.{Matchers, FlatSpec}

/**
 * Basic property entity validation
 * Created by darcio on 9/25/16.
 */
class PropertyValidationTest  extends FlatSpec with Matchers {

  "An invalid property creation " should "fail by invalid beds range" in {
    a [IllegalArgumentException] should be thrownBy {
      getValidProperty().copy(beds = 0)
    }
  }
  it should "fail by invalid baths range" in {
    a [IllegalArgumentException] should be thrownBy {
      getValidProperty().copy(baths = 0)
    }
  }
  it should "fail by invalid size range" in {
    a [IllegalArgumentException] should be thrownBy {
      getValidProperty().copy(squareMeters = 19)
    }
  }
  it should "fail by invalid beds range again" in {
    a [IllegalArgumentException] should be thrownBy {
      getValidProperty().copy(beds = 6)
    }
  }
  it should "fail by invalid baths range again" in {
    a [IllegalArgumentException] should be thrownBy {
      getValidProperty().copy(baths = 5)
    }
  }
  it should "fail by invalid size range again" in {
    a [IllegalArgumentException] should be thrownBy {
      getValidProperty().copy(squareMeters = 241)
    }
  }

  def getValidProperty(): Property = Property(
    id = None,
    title = "nova casa teste",
    price = 234234,
    description = "nova casa teste desc",
    lat = 200,
    long = 300,
    beds = 3,
    baths = 3,
    squareMeters = 200,
    provinces = None
  )
}
