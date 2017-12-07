package utensil

import models.OriginalImage
import nyhx.Images
import org.scalatest.{FunSuite, WordSpec}

class FindPicTest extends WordSpec {
  val userDir = System.getProperty("user.dir").replaceAll("\\\\", "/")

  val original = OriginalImage(s"$userDir/images-original/room.png")
  "wdj" must {
    "wdj" in {
      val goal = Images.Wdj.wuDouJi.toGoal
      val result = FindPic(original, goal)
      println(result.similarity -> result.point)
      assert(result.isFind)
    }
    "patten" in {
      val goal = Images.Wdj.wuDouJi.toGoal
      val result = FindPic(original, goal, patten = FindPic.Patten.Edge)
      println(result.similarity -> result.point)
      assert(result.isFind)
    }
    "sen shen" in {
      val goal = Images.Wdj.shenShen.toGoal
      val result = FindPic(original, goal)
      println(result.similarity -> result.point)
      assert(result.noFind)
    }
  }
}
