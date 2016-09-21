package com.vr.challenge.entity

case class Province(boundaries: Boundaries )

case class Corner( x: BigDecimal, y: BigDecimal)

case class Boundaries( upperLeft: Corner, bottomRight: Corner)

case class Property(   id                               : Option[Int],
                       title                                : String,
                       price                                : BigDecimal,
                       description                                : String,
                       lat                                : BigDecimal,
                       long                               : BigDecimal,
                       beds                               : Int,
                       baths                                : Int,
                       squareMeters                               : Double,
                       provinces                                : Set[String]
                     )

case class PropertyLot( totalProperties: Int,
                        properties: List[Property])