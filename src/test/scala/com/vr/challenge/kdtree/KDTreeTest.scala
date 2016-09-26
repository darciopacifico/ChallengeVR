
package com.vr.challenge.kdtree

import com.vr.challenge.Boot
import com.vr.challenge.protocol.PropertyProtocol.{Property, PropertyLot}
import edu.wlu.cs.levy.CG.KDTree
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.io.Source


/**
 * Basic tests for KDTree range and insertion functionalities
 * Created by darcio on 9/24/16.
 */
class KDTreeTest extends FlatSpec with Matchers {

  implicit val df = org.json4s.DefaultFormats

  val kdTree = new KDTree[mutable.Set[Property]](2)
  val propLot = Boot.loadPropertyLot

  assert(propLot.totalProperties === propLot.properties.size)
  assert(propLot.properties.size > 100)

  "Property file" should "be completely loaded into KDTree" in {
    propLot.properties.foreach { property =>
      val latLong = Array(property.lat.toDouble, property.long.toDouble)
      val setOfProps = getSetOfProperties(kdTree, latLong)
      setOfProps += property
    }
    val totalProperties = kdTree.range(Array(0, 0), Array(10000, 10000)).asScala.flatMap(x => x)
    propLot.properties.length should be(totalProperties.size)
  }

  it should "find a specific property by its latLong coordinates" in {
    val properties = kdTree.range(Array(119, 530), Array(123, 535)).asScala.flatMap(x => x)
    properties.size should be(1)
    properties.head.id should be(Some(5577))
  }

  it should "find more than one property in a specific latlog" in {
    val properties = kdTree.range(Array(746, 835), Array(746, 835)).asScala.flatMap(x => x)
    properties.size should be(2)
    properties.map(_.id) should contain theSameElementsAs Vector(Some(465), Some(699))
  }


  /**
   * Get set from KDtree for tests
   * @param kdTree
   * @param coords
   * @return
   */
  def getSetOfProperties(kdTree: KDTree[mutable.Set[Property]], coords: Array[Double]) =
    kdTree.search(coords) match {
      case null =>
        val newPropSet = mutable.Set[Property]()
        kdTree.insert(coords, newPropSet)
        newPropSet

      case propSet: mutable.Set[Property] =>
        propSet
    }
}
