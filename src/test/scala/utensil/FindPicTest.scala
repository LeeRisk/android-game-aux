package utensil

import models.OriginalImage
import nyhx.Images
import org.scalatest.{FunSuite, WordSpec}

class FindPicTest extends WordSpec {
  val userDir = System.getProperty("user.dir").replaceAll("\\\\", "/")

  "wdj" must {
    "wdj" in {
      val original = OriginalImage(s"$userDir/src/test/resources/screen-gruan.png")
      val goal = Images.Wdj.wuDouJi.toGoal
      val result = FindPic(original, goal)
      println(result.similarity -> result.point)
      assert(result.isFind)
    }
    "sen shen" in {
      val original = OriginalImage(s"$userDir/src/test/resources/screen-gruan.png")
      val goal = Images.Wdj.shenShen.toGoal
      val result = FindPic(original, goal)
      println(result.similarity -> result.point)
      assert(result.noFind)
    }
  }
}
